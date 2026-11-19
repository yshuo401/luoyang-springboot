package com.luoyang.tourism.module.auth.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppUser {
    private Long id;
    private String openid;
    private String unionid;
    private String nickname;
    private Long avatarFileId;
    private Integer gender;
    private String city;
    private Integer status;
    private Integer tokenVersion;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    private Integer isDeleted;
}
