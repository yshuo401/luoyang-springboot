package com.luoyang.tourism.security.jwt;

/**
 * TokenAudience 安全组件。
 * <p>负责 JWT、权限、限流或请求安全校验。</p>
 */
public enum TokenAudience {
    APP_USER("user-wx"),
    ADMIN("admin-web");

    private final String value;

    TokenAudience(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
