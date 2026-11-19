package com.luoyang.tourism.common.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * TimeUtils 通用工具类。
 */
public final class TimeUtils {
    public static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private TimeUtils() {
    }

    public static OffsetDateTime offset(LocalDateTime value) {
        return value == null ? null : value.atZone(ZONE).toOffsetDateTime();
    }
}
