package com.luoyang.tourism.module.auth.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * AdminUser 数据库实体。
 * <p>字段与数据表 admin_user 对应，不直接作为接口响应。</p>
 */
@Data
public class AdminUser {
    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private Long avatarFileId;
    private String email;
    private String phone;
    private Integer status;
    private Integer tokenVersion;
    private Integer failedLoginCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer version;
    private Integer isDeleted;
}
