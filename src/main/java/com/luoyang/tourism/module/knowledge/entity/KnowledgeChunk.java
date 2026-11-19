package com.luoyang.tourism.module.knowledge.entity;

import lombok.Data;
import java.time.LocalDateTime;

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
