package com.luoyang.tourism.module.auth.pojo.vo;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 认证接口响应模型。
 */
public final class AuthVos {
    private AuthVos() {
    }

    public record AppUserVO(String id, String nickname, String avatarUrl, Integer gender, String city,
                            OffsetDateTime createdAt) {
    }

    public record AppLoginVO(String accessToken, long expiresIn, AppUserVO user) {
    }

    public record AdminProfileVO(String id, String username, String nickname, String avatarUrl,
                                 List<String> roles, List<String> permissions) {
    }

    public record AdminLoginVO(String accessToken, long expiresIn, AdminProfileVO admin) {
    }
}