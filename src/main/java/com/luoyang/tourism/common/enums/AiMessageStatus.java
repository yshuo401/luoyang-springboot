package com.luoyang.tourism.common.enums;

/**
 * AiMessageStatus 通用组件。
 * <p>为各业务模块提供统一的基础能力。</p>
 */

public enum AiMessageStatus {
    PROCESSING(0), GROUNDED(1), FAILED(2), INSUFFICIENT(3), LIMITED(4);

    private final int code;

    AiMessageStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
