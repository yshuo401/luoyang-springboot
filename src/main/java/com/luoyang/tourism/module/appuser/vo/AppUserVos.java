package com.luoyang.tourism.module.appuser.vo;

import com.luoyang.tourism.module.content.vo.ContentVos;

import java.time.OffsetDateTime;
import java.util.List;

public final class AppUserVos {
    private AppUserVos() {
    }

    public record PreferenceVO(List<String> categoryIds, List<String> tagIds,
                               List<String> foodCategoryIds, OffsetDateTime updatedAt) {
    }

    public record FavoriteTargetVO(String id, String name, String coverUrl) {
    }

    public record FavoriteVO(String id, String bizType, String bizId, FavoriteTargetVO target,
                             boolean available, OffsetDateTime createdAt) {
    }

    public record RecommendationVO(ContentVos.SpotCard spot, double score, String reason,
                                   String algorithmVersion, String traceId) {
    }
}
