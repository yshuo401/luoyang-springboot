package com.luoyang.tourism.module.content.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * ScenicCategory 数据库实体。
 * <p>字段与数据表 scenic_category 对应，不直接作为接口响应。</p>
 */
@Data
public class ScenicCategory {
    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private Long iconFileId;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer version;
    private Integer isDeleted;
}
