package com.luoyang.tourism.module.appuser.pojo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * UserPreference 数据库实体。
 * <p>字段与数据表 user_preference 对应，不直接作为接口响应。</p>
 */
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
