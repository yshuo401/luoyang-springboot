package com.luoyang.tourism.common.api;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * ApiResponse 通用组件。
 * <p>为各业务模块提供统一的基础能力。</p>
 */
public record ApiResponse<T>(
        int code,
        String message,
        T data,
        String traceId,
        OffsetDateTime timestamp
) {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data, com.luoyang.tourism.common.trace.TraceIds.current(),
                OffsetDateTime.now(ZONE));
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, com.luoyang.tourism.common.trace.TraceIds.current(),
                OffsetDateTime.now(ZONE));
    }
}
