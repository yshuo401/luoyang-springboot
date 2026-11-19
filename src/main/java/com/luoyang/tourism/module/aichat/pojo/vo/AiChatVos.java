package com.luoyang.tourism.module.aichat.pojo.vo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * AiChatVos 视图对象，用于接口响应展示。
 */
public final class AiChatVos {
    private AiChatVos() {
    }

    public record ConversationVO(String conversationId, String title, OffsetDateTime createdAt) {
    }

    public record ConversationItemVO(String conversationId, String title, OffsetDateTime lastMessageAt,
                                     Integer messageCount, String lastMessagePreview) {
    }

    public record SourceVO(String sourceType, String sourceId, String chunkId, String title,
                           String snippet, BigDecimal score, Integer rankNo, String deepLink) {
    }

    public record MessageVO(String messageId, String role, String content, Integer status,
                            boolean grounded, List<SourceVO> sources, OffsetDateTime createdAt) {
    }

    public record MessagePageVO(List<MessageVO> records, Integer nextCursor) {
    }

    public record ChatVO(String conversationId, String userMessageId, String assistantMessageId,
                         String answer, boolean grounded, boolean insufficient, String insufficientReason,
                         List<SourceVO> sources, List<String> suggestedQuestions, String modelName,
                         int latencyMs, String traceId) {
    }
}
