package com.luoyang.tourism.module.file.vo;

public record FileVO(
        String fileId,
        String originalName,
        String contentType,
        long fileSize,
        Integer status,
        String url
) {
}
