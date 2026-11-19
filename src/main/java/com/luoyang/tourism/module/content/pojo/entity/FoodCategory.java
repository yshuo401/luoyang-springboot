package com.luoyang.tourism.module.content.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * FoodCategory 数据库实体。
 * <p>字段与数据表 food_category 对应，不直接作为接口响应。</p>
 */
@Data
public class FoodCategory {
    private Long id;
    private String name;
    private String code;
    private Long iconFileId;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer version;
    private Integer isDeleted;
}
