package com.luoyang.tourism.module.knowledge.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * KnowledgeChunk 数据库实体。
 * <p>字段与数据表 knowledge_chunk 对应，不直接作为接口响应。</p>
 */
@Data
public class KnowledgeChunk {
    private Long id;
    private Long documentId;
    private Integer chunkIndex;
    private String vectorId;
    private String content;
    private String contentHash;
    private Integer charCount;
    private String metadata;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
