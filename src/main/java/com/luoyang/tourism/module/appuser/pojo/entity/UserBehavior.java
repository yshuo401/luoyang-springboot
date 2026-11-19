package com.luoyang.tourism.module.appuser.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * UserBehavior 数据库实体。
 * <p>字段与数据表 user_behavior 对应，不直接作为接口响应。</p>
 */
@Data
public class UserBehavior {
    private Long id;
    private Long userId;
    private Integer behaviorType;
    private Integer bizType;
    private Long bizId;
    private String keyword;
    private Integer durationMs;
    private String source;
    private String metadata;
    private String traceId;
    private LocalDateTime createdAt;
}
