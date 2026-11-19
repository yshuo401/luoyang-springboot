package com.luoyang.tourism.module.knowledge.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * KnowledgeDtos 数据传输对象，用于接口请求参数和内部契约。
 */
public final class KnowledgeDtos {
    private KnowledgeDtos() {
    }

    public record ReindexRequest(@NotBlank @Size(max = 128) String idempotencyKey) {
    }
}
