package com.luoyang.tourism.module.auth.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * AppUser 数据库实体。
 * <p>字段与数据表 app_user 对应，不直接作为接口响应。</p>
 */
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
