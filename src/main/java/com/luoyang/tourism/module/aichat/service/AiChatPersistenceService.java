package com.luoyang.tourism.module.aichat.service;

import com.luoyang.tourism.common.enums.AiMessageStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyang.tourism.integration.ai.AiDtos;
import com.luoyang.tourism.module.aichat.entity.AiConversation;
import com.luoyang.tourism.module.aichat.entity.AiMessage;
import com.luoyang.tourism.module.aichat.entity.AiMessageSource;
import com.luoyang.tourism.module.aichat.mapper.AiChatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChatPersistenceService {
    private final AiChatMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public PreparedMessages startNew(AiConversation conversation, Long userId, String content,
                                     String clientMessageId, String traceId) {
        int userSequence = mapper.nextSequence(conversation.getId());
        AiMessage userMessage = new AiMessage();
        userMessage.setConversationId(conversation.getId());
        userMessage.setUserId(userId);
        userMessage.setSequenceNo(userSequence);
        userMessage.setRole("user");
        userMessage.setContent(content);
        userMessage.setStatus(AiMessageStatus.GROUNDED.code());
        userMessage.setClientMessageId(clientMessageId);
        userMessage.setTraceId(traceId);
        userMessage.setGrounded(0);
        mapper.insertMessage(userMessage);

        AiMessage assistant = new AiMessage();
        assistant.setConversationId(conversation.getId());
        assistant.setUserId(null);
        assistant.setSequenceNo(userSequence + 1);
        assistant.setRole("assistant");
        assistant.setContent("正在思考…");
        assistant.setStatus(AiMessageStatus.PROCESSING.code());
        assistant.setTraceId(traceId);
        assistant.setGrounded(0);
        mapper.insertMessage(assistant);
        mapper.addConversationMessages(conversation.getId(), 2, LocalDateTime.now());
        return new PreparedMessages(userMessage, assistant);
    }

    public void retry(AiMessage assistant, String traceId) {
        mapper.resetAssistantForRetry(assistant.getId(), traceId);
    }

    @Transactional
    public void complete(Long conversationId, AiMessage assistant, AiDtos.ChatResult result,
                         List<AiMessageSource> sources) {
        boolean grounded = result.grounded() && result.sources() != null && !result.sources().isEmpty();
        assistant.setContent(result.answer());
        assistant.setStatus(result.insufficient() ? AiMessageStatus.INSUFFICIENT.code()
                : grounded ? AiMessageStatus.GROUNDED.code() : AiMessageStatus.LIMITED.code());
        assistant.setModelName(result.modelName());
        assistant.setLatencyMs(result.latencyMs());
        assistant.setGrounded(grounded ? 1 : 2);
        assistant.setTokenUsage(result.tokenUsage() == null ? null : writeTokenUsage(result.tokenUsage()));
        mapper.updateAssistantMessage(assistant);
        for (AiMessageSource source : sources) {
            source.setMessageId(assistant.getId());
            mapper.insertSource(source);
        }
        mapper.addConversationMessages(conversationId, 0, LocalDateTime.now());
    }

    @Transactional
    public void fail(Long conversationId, Long assistantId, Integer latencyMs) {
        AiMessage assistant = mapper.selectMessageById(assistantId);
        if (assistant == null) {
            return;
        }
        assistant.setContent("智能问答暂时不可用，请稍后重试。");
        assistant.setStatus(AiMessageStatus.FAILED.code());
        assistant.setModelName(null);
        assistant.setTokenUsage(null);
        assistant.setLatencyMs(latencyMs);
        assistant.setGrounded(2);
        mapper.updateAssistantMessage(assistant);
        mapper.addConversationMessages(conversationId, 0, LocalDateTime.now());
    }

    private String writeTokenUsage(java.util.Map<String, Object> tokenUsage) {
        try {
            return objectMapper.writeValueAsString(tokenUsage);
        } catch (Exception exception) {
            return null;
        }
    }

    public record PreparedMessages(AiMessage userMessage, AiMessage assistantMessage) {
    }
}
