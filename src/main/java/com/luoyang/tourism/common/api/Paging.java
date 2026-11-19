package com.luoyang.tourism.common.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Paging 通用分页工具。
 */
public final class Paging {
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 50;

    private Paging() {
    }

    public static int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    public static int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    public static long offset(int page, int pageSize) {
        return (long) (normalizePage(page) - 1) * normalizePageSize(pageSize);
    }

    public static <T> PageResult<T> of(List<T> records, long total, Integer page, Integer pageSize) {
        return new PageResult<>(records == null ? new ArrayList<>() : records, total,
                normalizePage(page), normalizePageSize(pageSize));
    }
}
