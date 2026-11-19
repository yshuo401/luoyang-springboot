package com.luoyang.tourism.module.appuser.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * AppUserDtos 数据传输对象，用于接口请求参数和内部契约。
 */
public final class AppUserDtos {
    private AppUserDtos() {
    }

    public record ProfileUpdateRequest(
            @Size(max = 64) String nickname,
            Long avatarFileId,
            @Min(0) @Max(2) Integer gender,
            @Size(max = 64) String city
    ) {
    }

    public record PreferenceUpdateRequest(
            @Size(max = 20) List<Long> categoryIds,
            @Size(max = 20) List<Long> tagIds,
            @Size(max = 20) List<Long> foodCategoryIds
    ) {
    }

    public record FavoriteRequest(
            @NotBlank String bizType,
            @NotNull Long bizId
    ) {
    }

    public record BehaviorRequest(
            @NotNull @Min(1) @Max(6) Integer behaviorType,
            @Min(1) @Max(3) Integer bizType,
            Long bizId,
            @Size(max = 128) String keyword,
            @Min(0) Integer durationMs,
            @Size(max = 32) String source,
            Map<String, Object> metadata
    ) {
    }
}
