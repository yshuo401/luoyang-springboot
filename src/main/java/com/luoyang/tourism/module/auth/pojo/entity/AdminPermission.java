package com.luoyang.tourism.module.auth.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * AdminPermission 数据库实体。
 * <p>字段与数据表 admin_permission 对应，不直接作为接口响应。</p>
 */
@Data
public class AdminPermission {
    private Long id;
    private Long parentId;
    private String permissionName;
    private String permissionCode;
    private Integer permissionType;
    private String routePath;
    private String componentPath;
    private String apiMethod;
    private String apiPath;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer version;
    private Integer isDeleted;
}
