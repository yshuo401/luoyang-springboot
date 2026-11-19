package com.luoyang.tourism.module.aichat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.enums.AiMessageStatus;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.trace.TraceIds;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.integration.ai.AiClient;
import com.luoyang.tourism.integration.ai.AiClientException;
import com.luoyang.tourism.integration.ai.AiDtos;
import com.luoyang.tourism.module.aichat.dto.AiChatDtos;
import com.luoyang.tourism.module.aichat.entity.AiConversation;
import com.luoyang.tourism.module.aichat.entity.AiMessage;
import com.luoyang.tourism.module.aichat.entity.AiMessageSource;
import com.luoyang.tourism.module.aichat.mapper.AiChatMapper;
import com.luoyang.tourism.module.aichat.vo.AiChatVos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {
    private static final Duration THREAD_LOCK_TTL = Duration.ofSeconds(45);
    private static final DefaultRedisScript<Long> RELEASE_LOCK = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final AiChatMapper mapper;
    private final AiChatPersistenceService persistence;
    private final AiClient aiClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public AiChatVos.ConversationVO createConversation(Long userId, AiChatDtos.CreateConversationRequest request) {
        String threadId = UlidCreator.getUlid().toString();
        String title = request.title() == null || request.title().isBlank() ? "新会话" : request.title().trim();
        if (title.length() > 100) {
            title = title.substring(0, 100);
        }
        AiConversation conversation = new AiConversation();
        conversation.setThreadId(threadId);
        conversation.setUserId(userId);
        conversation.setTitle(title);
        mapper.insertConversation(conversation);
        AiConversation saved = mapper.selectConversationByThreadId(threadId);
        return new AiChatVos.ConversationVO(saved.getThreadId(), saved.getTitle(),
                TimeUtils.offset(saved.getCreatedAt()));
    }

    public PageResult<AiChatVos.ConversationItemVO> listConversations(Long userId,
                                                                       Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<AiChatVos.ConversationItemVO> records = mapper.selectConversations(userId,
                        Paging.offset(safePage, safeSize), safeSize)
                .stream().map(value -> new AiChatVos.ConversationItemVO(value.getThreadId(), value.getTitle(),
                        TimeUtils.offset(value.getLastMessageAt()), value.getMessageCount(),
                        value.getLastMessagePreview())).toList();
        return Paging.of(records, mapper.countConversations(userId), safePage, safeSize);
    }

    public AiChatVos.MessagePageVO messages(Long userId, String threadId, Integer cursor, Integer limit) {
        AiConversation conversation = ownedConversation(userId, threadId);
        int safeLimit = limit == null ? 20 : Math.min(Math.max(limit, 1), 50);
        List<AiMessage> rows = mapper.selectMessages(conversation.getId(), cursor, safeLimit + 1);
        boolean hasMore = rows.size() > safeLimit;
        List<AiMessage> page = hasMore ? rows.subList(0, safeLimit) : rows;
        Map<Long, List<AiMessageSource>> sources = sourcesFor(page);
        List<AiChatVos.MessageVO> records = page.stream().map(message ->
                messageVo(message, sources.getOrDefault(message.getId(), List.of()))).toList();
        Integer nextCursor = hasMore && !page.isEmpty() ? page.get(page.size() - 1).getSequenceNo() : null;
        return new AiChatVos.MessagePageVO(records, nextCursor);
    }

    public void deleteConversation(Long userId, String threadId) {
        AiConversation conversation = ownedConversation(userId, threadId);
        try {
            aiClient.resetCheckpoint(threadId, TraceIds.current());
        } catch (RuntimeException exception) {
            log.warn("checkpoint reset failed conversationId={} traceId={}", threadId, TraceIds.current());
        }
        if (mapper.softDeleteConversation(conversation.getId(), userId) != 1) {
            throw BizException.notFound();
        }
    }

    public AiChatVos.ChatVO chat(Long userId, AiChatDtos.ChatRequest request, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()
                && !idempotencyKey.equals(request.clientMessageId())) {
            throw new BizException(ResultCode.BAD_REQUEST, "Idempotency-Key 与 clientMessageId 不一致");
        }
        AiConversation conversation = ownedConversation(userId, request.conversationId());
        String content = request.message().trim();
        if (content.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "问题不能为空");
        }
        String traceId = TraceIds.current();
        String lockKey = "tourism:spring:ai:thread:" + conversation.getThreadId() + ":lock";
        if (!acquireLock(lockKey, traceId)) {
            throw new BizException(ResultCode.AI_BUSY);
        }
        long startedAt = System.currentTimeMillis();
        try {
            AiMessage userMessage = mapper.selectUserMessageByClientId(conversation.getId(), request.clientMessageId());
            if (userMessage != null) {
                if (!userMessage.getContent().equals(content)) {
                    throw new BizException(ResultCode.IDEMPOTENCY_CONFLICT);
                }
                AiMessage assistant = mapper.selectAssistantBySequence(conversation.getId(),
                        userMessage.getSequenceNo() + 1);
                if (assistant == null) {
                    throw new BizException(ResultCode.DATA_INCONSISTENCY);
                }
                if (assistant.getStatus() == AiMessageStatus.PROCESSING.code()) {
                    throw new BizException(ResultCode.AI_BUSY);
                }
                if (assistant.getStatus() != AiMessageStatus.FAILED.code()) {
                    return existingChat(conversation, userMessage, assistant);
                }
                persistence.retry(assistant, traceId);
                return invokeAndComplete(conversation, userMessage, assistant, content, request.clientMessageId(),
                        traceId, startedAt);
            }

            AiChatPersistenceService.PreparedMessages prepared = persistence.startNew(conversation, userId,
                    content, request.clientMessageId(), traceId);
            return invokeAndComplete(conversation, prepared.userMessage(), prepared.assistantMessage(),
                    content, request.clientMessageId(), traceId, startedAt);
        } finally {
            releaseLock(lockKey, traceId);
        }
    }

    public List<AiChatVos.MessageVO> recentMessages(String threadId, Integer limit) {
        AiConversation conversation = mapper.selectConversationByThreadId(threadId);
        if (conversation == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        int safeLimit = limit == null ? 20 : Math.min(Math.max(limit, 1), 50);
        List<AiMessage> rows = mapper.selectRecentMessages(conversation.getId(), safeLimit);
        Map<Long, List<AiMessageSource>> sources = sourcesFor(rows);
        return rows.stream().map(message -> messageVo(message,
                sources.getOrDefault(message.getId(), List.of()))).toList();
    }

    private AiChatVos.ChatVO invokeAndComplete(AiConversation conversation, AiMessage userMessage,
                                               AiMessage assistant, String content, String clientMessageId,
                                               String traceId, long startedAt) {
        AiDtos.ChatRequest request = new AiDtos.ChatRequest(conversation.getThreadId(),
                Long.toString(conversation.getUserId()), content, clientMessageId, traceId, List.of());
        try {
            AiDtos.ChatResult result;
            try {
                result = aiClient.chat(request, traceId);
            } catch (AiClientException exception) {
                if (exception.code() != 40410) {
                    throw exception;
                }
                List<AiDtos.RecoveryMessage> history = recoveryHistory(conversation.getId(),
                        userMessage.getSequenceNo());
                result = aiClient.chat(new AiDtos.ChatRequest(conversation.getThreadId(),
                        Long.toString(conversation.getUserId()), content, clientMessageId, traceId, history), traceId);
            }
            List<AiMessageSource> sources = toSources(result.sources());
            persistence.complete(conversation.getId(), assistant, result, sources);
            return new AiChatVos.ChatVO(conversation.getThreadId(), Long.toString(userMessage.getId()),
                    Long.toString(assistant.getId()), result.answer(),
                    result.grounded() && result.sources() != null && !result.sources().isEmpty(),
                    result.insufficient(), result.insufficientReason(),
                    result.sources() == null ? List.of() : result.sources().stream().map(this::sourceVo).toList(),
                    result.suggestedQuestions() == null ? List.of() : result.suggestedQuestions(),
                    result.modelName(), result.latencyMs(), traceId);
        } catch (AiClientException exception) {
            int latency = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startedAt);
            persistence.fail(conversation.getId(), assistant.getId(), latency);
            throw new BizException(mapError(exception.code()), exception.getMessage());
        } catch (BizException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            int latency = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startedAt);
            persistence.fail(conversation.getId(), assistant.getId(), latency);
            throw new BizException(ResultCode.AI_UNAVAILABLE);
        }
    }

    private AiChatVos.ChatVO existingChat(AiConversation conversation, AiMessage userMessage, AiMessage assistant) {
        List<AiMessageSource> sources = mapper.selectSourcesByMessageId(assistant.getId());
        return new AiChatVos.ChatVO(conversation.getThreadId(), Long.toString(userMessage.getId()),
                Long.toString(assistant.getId()), assistant.getContent(),
                Integer.valueOf(1).equals(assistant.getGrounded()),
                assistant.getStatus() == AiMessageStatus.INSUFFICIENT.code(),
                assistant.getStatus() == AiMessageStatus.INSUFFICIENT.code()
                        ? "RETRIEVAL_EMPTY_OR_BELOW_THRESHOLD" : null,
                sources.stream().map(this::sourceVo).toList(), List.of(), assistant.getModelName(),
                assistant.getLatencyMs() == null ? 0 : assistant.getLatencyMs(), assistant.getTraceId());
    }

    private AiConversation ownedConversation(Long userId, String threadId) {
        AiConversation conversation = mapper.selectConversationByThreadId(threadId);
        if (conversation == null) {
            throw BizException.notFound();
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BizException(ResultCode.RESOURCE_NOT_OWNED);
        }
        return conversation;
    }

    private boolean acquireLock(String key, String value) {
        try {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, value, THREAD_LOCK_TTL);
            return Boolean.TRUE.equals(acquired);
        } catch (RuntimeException exception) {
            throw new BizException(ResultCode.AI_UNAVAILABLE);
        }
    }

    private void releaseLock(String key, String value) {
        try {
            redisTemplate.execute(RELEASE_LOCK, List.of(key), value);
        } catch (RuntimeException ignored) {
            // Lock expires automatically.
        }
    }

    private List<AiDtos.RecoveryMessage> recoveryHistory(Long conversationId, int currentSequence) {
        List<AiMessage> rows = mapper.selectRecentMessages(conversationId, 20);
        List<AiDtos.RecoveryMessage> history = new ArrayList<>();
        for (AiMessage row : rows) {
            if (row.getSequenceNo() >= currentSequence) {
                continue;
            }
            if (!Set.of("user", "assistant").contains(row.getRole())) {
                continue;
            }
            String content = row.getContent();
            if (content != null && content.length() > 4000) {
                content = content.substring(0, 4000);
            }
            history.add(new AiDtos.RecoveryMessage(row.getRole(), content));
        }
        if (history.size() > 10) {
            history = history.subList(history.size() - 10, history.size());
        }
        return history;
    }

    private Map<Long, List<AiMessageSource>> sourcesFor(List<AiMessage> messages) {
        if (messages.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = messages.stream().map(AiMessage::getId).toList();
        Map<Long, List<AiMessageSource>> grouped = new LinkedHashMap<>();
        for (AiMessageSource source : mapper.selectSourcesByMessageIds(ids)) {
            grouped.computeIfAbsent(source.getMessageId(), key -> new ArrayList<>()).add(source);
        }
        return grouped;
    }

    private AiChatVos.MessageVO messageVo(AiMessage message, List<AiMessageSource> sources) {
        return new AiChatVos.MessageVO(Long.toString(message.getId()), message.getRole(), message.getContent(),
                message.getStatus(), Integer.valueOf(1).equals(message.getGrounded()),
                sources.stream().map(this::sourceVo).toList(), TimeUtils.offset(message.getCreatedAt()));
    }

    private AiChatVos.SourceVO sourceVo(AiMessageSource source) {
        return new AiChatVos.SourceVO(sourceTypeName(source.getSourceType()), stringId(source.getSourceId()),
                stringId(source.getChunkId()), source.getSourceTitle(), source.getSnippet(), source.getScore(),
                source.getRankNo(), source.getSourceUrl());
    }

    private AiChatVos.SourceVO sourceVo(AiDtos.Source source) {
        return new AiChatVos.SourceVO(source.sourceType(), source.sourceId(), source.chunkId(), source.title(),
                source.snippet(), source.score() == null ? null : BigDecimal.valueOf(source.score()),
                source.rankNo(), source.deepLink());
    }

    private List<AiMessageSource> toSources(List<AiDtos.Source> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<AiMessageSource> sources = new ArrayList<>();
        for (AiDtos.Source item : items) {
            AiMessageSource source = new AiMessageSource();
            source.setSourceType(sourceTypeCode(item.sourceType()));
            source.setSourceId(parseId(item.sourceId()));
            source.setChunkId(parseId(item.chunkId()));
            source.setSourceTitle(item.title() == null ? "未命名来源" : item.title());
            source.setSourceUrl(item.deepLink());
            source.setSnippet(item.snippet());
            source.setScore(item.score() == null ? null : BigDecimal.valueOf(item.score()));
            source.setRankNo(item.rankNo());
            source.setMetadata("{}");
            sources.add(source);
        }
        return sources;
    }

    private int sourceTypeCode(String value) {
        if (value == null) {
            return 1;
        }
        return switch (value.toUpperCase(java.util.Locale.ROOT)) {
            case "SPOT" -> 2;
            case "GUIDE" -> 3;
            case "FOOD" -> 4;
            case "RECOMMENDATION" -> 5;
            default -> 1;
        };
    }

    private String sourceTypeName(Integer value) {
        if (value == null) {
            return "KNOWLEDGE";
        }
        return switch (value) {
            case 2 -> "SPOT";
            case 3 -> "GUIDE";
            case 4 -> "FOOD";
            case 5 -> "RECOMMENDATION";
            default -> "KNOWLEDGE";
        };
    }

    private ResultCode mapError(int code) {
        return switch (code) {
            case 50302 -> ResultCode.MODEL_TIMEOUT;
            case 50301 -> ResultCode.VECTOR_UNAVAILABLE;
            case 40903 -> ResultCode.AI_BUSY;
            case 40000 -> ResultCode.BAD_REQUEST;
            default -> ResultCode.AI_UNAVAILABLE;
        };
    }

    private Long parseId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String stringId(Long value) {
        return value == null ? null : Long.toString(value);
    }
}
