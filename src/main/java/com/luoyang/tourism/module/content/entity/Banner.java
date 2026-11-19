package com.luoyang.tourism.module.content.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Banner {
    private Long id;
    private String title;
    private String subtitle;
    private Long imageFileId;
    private Integer linkType;
    private Long targetId;
    private String targetPath;
    private String position;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer version;
    private Integer isDeleted;
}
