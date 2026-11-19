package com.luoyang.tourism.module.knowledge.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeDocument {
    private Long id;
    private String title;
    private String category;
    private Long fileId;
    private Integer docType;
    private Integer versionNo;
    private String checksum;
    private Integer status;
    private Integer indexStatus;
    private String indexError;
    private Integer chunkCount;
    private String sourceUrl;
    private LocalDateTime effectiveAt;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer version;
    private Integer isDeleted;
}
