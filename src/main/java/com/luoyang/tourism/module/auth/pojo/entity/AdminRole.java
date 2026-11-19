package com.luoyang.tourism.module.auth.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * AdminRole 数据库实体。
 * <p>字段与数据表 admin_role 对应，不直接作为接口响应。</p>
 */
@Data
public class AdminRole {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer version;
    private Integer isDeleted;
}
