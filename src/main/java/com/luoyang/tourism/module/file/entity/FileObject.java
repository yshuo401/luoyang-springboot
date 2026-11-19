package com.luoyang.tourism.module.file.entity;

import lombok.Data;
import java.time.LocalDateTime;

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
