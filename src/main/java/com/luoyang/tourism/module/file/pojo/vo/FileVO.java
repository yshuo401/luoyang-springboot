package com.luoyang.tourism.module.file.pojo.vo;

/**
 * FileVO 视图对象。
 * <p>用于接口响应展示，BigInt 主键统一按字符串返回。</p>
 */
public record FileVO(
        String fileId,
        String originalName,
        String contentType,
        long fileSize,
        Integer status,
        String url
) {
}
