package com.luoyang.tourism.common.trace;

import org.slf4j.MDC;

/**
 * TraceIds 链路追踪工具。
 */
public final class TraceIds {
    public static final String MDC_KEY = "traceId";
    public static final String HEADER = "X-Request-Id";

    private TraceIds() {
    }

    public static String current() {
        String value = MDC.get(MDC_KEY);
        return value == null ? "" : value;
    }
}
