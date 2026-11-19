package com.luoyang.tourism.module.content.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * FoodImage 数据库实体。
 * <p>字段与数据表 food_image 对应，不直接作为接口响应。</p>
 */
@Data
public class FoodImage {
    private Long id;
    private Long foodId;
    private Long fileId;
    private String caption;
    private Integer sortOrder;
    private Integer isCover;
    private LocalDateTime createdAt;
    private Long createdBy;
    private Integer isDeleted;
    private String url;
}
