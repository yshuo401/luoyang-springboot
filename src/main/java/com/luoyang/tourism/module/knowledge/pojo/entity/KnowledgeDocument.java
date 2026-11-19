package com.luoyang.tourism.module.knowledge.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * KnowledgeDocument 数据库实体。
 * <p>字段与数据表 knowledge_document 对应，不直接作为接口响应。</p>
 */
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
