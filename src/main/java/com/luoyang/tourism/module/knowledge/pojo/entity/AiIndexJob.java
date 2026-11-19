package com.luoyang.tourism.module.knowledge.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * AiIndexJob 数据库实体。
 * <p>字段与数据表 ai_index_job 对应，不直接作为接口响应。</p>
 */
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
