package cn.luoyang.tourism.interfaces.rest;

import java.util.UUID;

/**
 * Stable envelope shared by the HTTP API. Business modules can add typed data
 * without changing the top-level response shape consumed by the web clients.
 */
public record ApiResponse<T>(int code, String message, T data, String requestId) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data, UUID.randomUUID().toString());
    }
}
