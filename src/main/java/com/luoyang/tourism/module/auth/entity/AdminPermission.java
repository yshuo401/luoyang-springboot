package com.luoyang.tourism.module.auth.entity;

import lombok.Data;
import java.time.LocalDateTime;

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
