package com.luoyang.tourism.module.file.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * FileObject 数据库实体。
 * <p>字段与数据表 file_object 对应，不直接作为接口响应。</p>
 */
@Data
public class FileObject {
    private Long id;
    private String bizScene;
    private String originalName;
    private String objectKey;
    private String bucket;
    private String contentType;
    private String extension;
    private Long fileSize;
    private String sha256;
    private String etag;
    private String provider;
    private Integer visibility;
    private Integer status;
    private Integer uploadedByType;
    private Long uploadedBy;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
