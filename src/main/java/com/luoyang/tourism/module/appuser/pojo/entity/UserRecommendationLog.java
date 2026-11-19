package com.luoyang.tourism.module.appuser.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * UserRecommendationLog 数据库实体。
 * <p>字段与数据表 user_recommendation_log 对应，不直接作为接口响应。</p>
 */
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
