package com.luoyang.tourism.common.api;

import java.util.List;

/**
 * PageResult 通用组件。
 * <p>为各业务模块提供统一的基础能力。</p>
 */
public record PageResult<T>(List<T> records, long total, long page, long pageSize) {
}
