package com.luoyang.tourism.security.jwt;

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
