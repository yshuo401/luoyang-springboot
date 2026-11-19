package com.luoyang.tourism.module.appuser.entity;

import lombok.Data;
import java.time.LocalDateTime;

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
