package com.luoyang.tourism.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * RequestUtils 通用工具类。
 */
public final class RequestUtils {
    private RequestUtils() {
    }

    public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
        if (ip.length() > 45 || !ip.matches("[0-9a-fA-F:.]+")) {
            return request.getRemoteAddr();
        }
        return ip;
    }
}
