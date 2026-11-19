package com.luoyang.tourism.module.knowledge.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiIndexJob {
    private Long id;
    private Long documentId;
    private Integer documentVersion;
    private Integer jobType;
    private Integer status;
    private String idempotencyKey;
    private String externalJobId;
    private Integer progress;
    private Integer totalChunks;
    private Integer processedChunks;
    private String errorMessage;
    private Long requestedBy;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
