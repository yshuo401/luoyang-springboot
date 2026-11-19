package com.luoyang.tourism.module.auth.entity;

import lombok.Data;
import java.time.LocalDateTime;

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
