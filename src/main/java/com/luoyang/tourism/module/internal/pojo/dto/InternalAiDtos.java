package com.luoyang.tourism.module.internal.pojo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * InternalAiDtos 数据传输对象，用于接口请求参数和内部契约。
 */
public final class InternalAiDtos {
    private InternalAiDtos() {
    }

    public record SpotSummary(String id, String name, String categoryName, String summary,
                              String address, String openTimeDesc, String ticketInfo,
                              Integer playDurationMinutes, List<String> tags, String deepLink) {
    }

    public record GuideSummary(String id, String title, String summary, Integer dayCount, String deepLink) {
    }

    public record FoodSummary(String id, String name, String categoryName, String summary,
                              String area, String priceRange, String deepLink) {
    }

    public record RecommendationItem(String id, String name, String categoryName, String summary,
                                     double score, String reason, String deepLink) {
    }

    public record RecoveryItem(String role, String content) {
    }

    public record RecommendationRequest(
            @NotNull Long userId,
            String scene,
            @Min(1) @Max(20) Integer limit,
            Long categoryId,
            @DecimalMin("-180") @DecimalMax("180") Double longitude,
            @DecimalMin("-90") @DecimalMax("90") Double latitude
    ) {
    }
}
