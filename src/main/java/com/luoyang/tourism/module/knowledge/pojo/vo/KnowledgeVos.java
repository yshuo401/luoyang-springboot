package com.luoyang.tourism.module.knowledge.pojo.vo;

import java.time.OffsetDateTime;

public final class KnowledgeVos {
    private KnowledgeVos() {
    }

    public record DocumentVO(String id, String title, String category, String fileId, Integer docType,
                             Integer versionNo, String checksum, Integer status, Integer indexStatus,
                             String indexError, Integer chunkCount, String sourceUrl,
                             OffsetDateTime effectiveAt, OffsetDateTime publishedAt,
                             Integer version, OffsetDateTime updatedAt) {
    }

    public record JobVO(String jobId, String documentId, Integer documentVersion, Integer jobType,
                        Integer status, String externalJobId, Integer progress, Integer totalChunks,
                        Integer processedChunks, String errorMessage, OffsetDateTime startedAt,
                        OffsetDateTime finishedAt, OffsetDateTime createdAt) {
    }

    public record UploadResult(String documentId, Integer versionNo, String jobId, Integer indexStatus,
                               String message) {
    }
}
