package com.luoyang.tourism.module.appuser.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserRecommendationLog {
    private Long id;
    private Long userId;
    private String scene;
    private String algorithmVersion;
    private String requestContext;
    private String itemsJson;
    private String traceId;
    private LocalDateTime createdAt;
}
