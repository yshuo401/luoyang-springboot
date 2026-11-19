package com.luoyang.tourism.module.appuser.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserPreference {
    private Long id;
    private Long userId;
    private Integer preferenceType;
    private Long preferenceKey;
    private BigDecimal weight;
    private Integer source;
    private LocalDateTime lastInteractedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
