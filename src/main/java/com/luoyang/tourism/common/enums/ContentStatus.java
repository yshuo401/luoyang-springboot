package com.luoyang.tourism.common.enums;

public enum ContentStatus {
    DRAFT(0), PUBLISHED(1), UNPUBLISHED(2), ARCHIVED(3);

    private final int code;

    ContentStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static ContentStatus of(Integer code) {
        if (code == null) {
            return DRAFT;
        }
        for (ContentStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unsupported content status: " + code);
    }
}
