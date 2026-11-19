package com.luoyang.tourism.module.content.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * ScenicImage 数据库实体。
 * <p>字段与数据表 scenic_image 对应，不直接作为接口响应。</p>
 */
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
