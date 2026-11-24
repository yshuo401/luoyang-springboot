package com.luoyang.tourism.module.knowledge.pojo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * KnowledgeDtos 数据传输对象，用于接口请求参数和内部契约。
 */
public final class KnowledgeDtos {
    private KnowledgeDtos() {
    }

    public record ReindexRequest(@NotBlank @Size(max = 128) String idempotencyKey) {
    }

    public record ChunkBatchRequest(
            @NotBlank @Size(max = 64) String documentId,
            @NotNull @Min(1) Integer documentVersion,
            @NotEmpty @Size(max = 200) List<@Valid ChunkItem> chunks
    ) {
    }

    public record ChunkItem(
            @NotNull @Min(0) Integer chunkIndex,
            @NotBlank @Size(min = 16, max = 128) String vectorId,
            @NotBlank String content,
            @NotBlank @Size(min = 16, max = 64) String contentHash,
            @NotNull @Min(1) Integer charCount,
            Map<String, Object> metadata
    ) {
    }

    public record ChunkBatchResult(String documentId, Integer documentVersion, int upsertedChunks) {
    }
}