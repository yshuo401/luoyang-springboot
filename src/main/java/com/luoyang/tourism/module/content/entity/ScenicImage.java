package com.luoyang.tourism.module.content.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScenicImage {
    private Long id;
    private Long spotId;
    private Long fileId;
    private String caption;
    private Integer sortOrder;
    private Integer isCover;
    private LocalDateTime createdAt;
    private Long createdBy;
    private Integer isDeleted;
    private String url;
}
