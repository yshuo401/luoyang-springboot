package com.luoyang.tourism.module.knowledge.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class KnowledgeDtos {
    private KnowledgeDtos() {
    }

    public record ReindexRequest(@NotBlank @Size(max = 128) String idempotencyKey) {
    }
}
