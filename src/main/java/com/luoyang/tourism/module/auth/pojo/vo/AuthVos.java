package com.luoyang.tourism.module.auth.pojo.vo;

import java.time.OffsetDateTime;
import java.util.List;

public final class AuthVos {
    private AuthVos() {
    }

    public record AppUserVO(String id, String nickname, String avatarUrl, Integer gender, String city,
                            OffsetDateTime createdAt) {
    }

    public record AppLoginVO(String accessToken, String refreshToken, long expiresIn, AppUserVO user) {
    }

    public record AdminProfileVO(String id, String username, String nickname, String avatarUrl,
                                 List<String> roles, List<String> permissions) {
    }

    public record AdminLoginVO(String accessToken, long expiresIn, AdminProfileVO admin) {
    }
}
