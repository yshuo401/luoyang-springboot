package com.luoyang.tourism.integration.ai;

import java.util.List;
import java.util.Map;

public final class AiDtos {
    private AiDtos() {
    }

    public record RecoveryMessage(String role, String content) {
    }

    public record ChatRequest(String threadId, String userId, String message, String clientMessageId,
                              String requestId, List<RecoveryMessage> recoveryHistory) {
    }

    public record Source(String sourceType, String sourceId, String chunkId, String title,
                         String snippet, Double score, int rankNo, String deepLink) {
    }

    public record ChatResult(String answer, boolean grounded, boolean insufficient,
                             String insufficientReason, List<Source> sources,
                             List<String> suggestedQuestions, String modelName, int latencyMs,
                             Map<String, Object> tokenUsage) {
    }

    public record IndexJobRequest(String documentId, int documentVersion, String fileId, String downloadUrl,
                                  int docType, String title, String category, String idempotencyKey) {
    }

    public record IndexJobResult(String jobId, String status, String externalJobId,
                                 Integer progress, Integer totalChunks, Integer processedChunks,
                                 String errorMessage) {
    }
}
