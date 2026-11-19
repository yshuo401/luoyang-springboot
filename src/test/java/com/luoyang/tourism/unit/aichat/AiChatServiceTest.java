package com.luoyang.tourism.unit.aichat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.enums.AiMessageStatus;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.integration.ai.AiClient;
import com.luoyang.tourism.integration.ai.AiDtos;
import com.luoyang.tourism.module.aichat.mapper.AiChatMapper;
import com.luoyang.tourism.module.aichat.pojo.dto.AiChatDtos;
import com.luoyang.tourism.module.aichat.pojo.entity.AiConversation;
import com.luoyang.tourism.module.aichat.pojo.entity.AiMessage;
import com.luoyang.tourism.module.aichat.service.AiChatPersistenceService;
import com.luoyang.tourism.module.aichat.service.impl.AiChatServiceImpl;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {
    @Mock
    private AiChatMapper mapper;
    @Mock
    private AiChatPersistenceService persistence;
    @Mock
    private AiClient aiClient;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private AiChatServiceImpl service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        service = new AiChatServiceImpl(mapper, persistence, aiClient, redisTemplate, new ObjectMapper());
    }

    @Test
    void completedClientMessageIsReturnedWithoutCallingAiAgain() {
        String threadId = UlidCreator.getUlid().toString();
        String clientId = "client-message-0001";
        String question = "龙门石窟适合玩多久？";
        AiConversation conversation = conversation(threadId, 100L);
        AiMessage user = message(10L, conversation.getId(), 1, "user", question, AiMessageStatus.GROUNDED.code());
        user.setClientMessageId(clientId);
        AiMessage assistant = message(11L, conversation.getId(), 2, "assistant",
                "建议安排三到四小时。[S1]", AiMessageStatus.GROUNDED.code());
        assistant.setGrounded(1);
        assistant.setModelName("qwen3.8-flash");
        assistant.setLatencyMs(900);
        assistant.setTraceId("trace-existing");

        when(mapper.selectConversationByThreadId(threadId)).thenReturn(conversation);
        when(mapper.selectUserMessageByClientId(conversation.getId(), clientId)).thenReturn(user);
        when(mapper.selectAssistantBySequence(conversation.getId(), 2)).thenReturn(assistant);
        when(mapper.selectSourcesByMessageId(11L)).thenReturn(List.of());

        var result = service.chat(100L, new AiChatDtos.ChatRequest(threadId, clientId, question), clientId);

        assertThat(result.answer()).contains("三到四小时");
        assertThat(result.assistantMessageId()).isEqualTo("11");
        assertThat(result.insufficient()).isFalse();
        verify(aiClient, never()).chat(any(), anyString());
        verify(persistence, never()).startNew(any(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void insufficientResultIsPersistedAndReturnedAsBusinessSuccess() {
        String threadId = UlidCreator.getUlid().toString();
        String clientId = "client-message-0002";
        String question = "请说说完全不存在的地方";
        AiConversation conversation = conversation(threadId, 100L);
        AiMessage user = message(20L, conversation.getId(), 1, "user", question, AiMessageStatus.GROUNDED.code());
        user.setClientMessageId(clientId);
        AiMessage assistant = message(21L, conversation.getId(), 2, "assistant", "正在思考…",
                AiMessageStatus.PROCESSING.code());
        when(mapper.selectConversationByThreadId(threadId)).thenReturn(conversation);
        when(mapper.selectUserMessageByClientId(conversation.getId(), clientId)).thenReturn(null);
        when(persistence.startNew(eq(conversation), eq(100L), eq(question), eq(clientId), anyString()))
                .thenReturn(new AiChatPersistenceService.PreparedMessages(user, assistant));
        when(aiClient.chat(any(), anyString())).thenReturn(new AiDtos.ChatResult(
                "资料不足，请换一种问法。", false, true, "RETRIEVAL_EMPTY_OR_BELOW_THRESHOLD",
                List.of(), List.of("洛阳有哪些适合半天游的景点？"), null, 80, null));

        var result = service.chat(100L, new AiChatDtos.ChatRequest(threadId, clientId, question), clientId);

        assertThat(result.insufficient()).isTrue();
        assertThat(result.grounded()).isFalse();
        assertThat(result.insufficientReason()).isEqualTo("RETRIEVAL_EMPTY_OR_BELOW_THRESHOLD");
        verify(persistence).complete(eq(conversation.getId()), eq(assistant), any(), eq(List.of()));
    }

    @Test
    void sameClientMessageWithDifferentContentIsRejected() {
        String threadId = UlidCreator.getUlid().toString();
        String clientId = "client-message-0003";
        AiConversation conversation = conversation(threadId, 100L);
        AiMessage user = message(30L, conversation.getId(), 1, "user", "旧问题",
                AiMessageStatus.GROUNDED.code());
        when(mapper.selectConversationByThreadId(threadId)).thenReturn(conversation);
        when(mapper.selectUserMessageByClientId(conversation.getId(), clientId)).thenReturn(user);

        BizException exception = catchThrowableOfType(() -> service.chat(100L,
                new AiChatDtos.ChatRequest(threadId, clientId, "新问题"), clientId), BizException.class);

        assertThat(exception.resultCode()).isEqualTo(ResultCode.IDEMPOTENCY_CONFLICT);
    }

    private AiConversation conversation(String threadId, Long userId) {
        AiConversation conversation = new AiConversation();
        conversation.setId(1L);
        conversation.setThreadId(threadId);
        conversation.setUserId(userId);
        conversation.setTitle("测试会话");
        return conversation;
    }

    private AiMessage message(Long id, Long conversationId, int sequence, String role, String content, int status) {
        AiMessage message = new AiMessage();
        message.setId(id);
        message.setConversationId(conversationId);
        message.setSequenceNo(sequence);
        message.setRole(role);
        message.setContent(content);
        message.setStatus(status);
        return message;
    }
}
