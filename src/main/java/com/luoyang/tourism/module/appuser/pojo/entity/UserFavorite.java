package com.luoyang.tourism.module.appuser.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * UserFavorite 数据库实体。
 * <p>字段与数据表 user_favorite 对应，不直接作为接口响应。</p>
 */
@Data
public class UserFavorite {
    private Long id;
    private Long userId;
    private Integer bizType;
    private Long bizId;
    private LocalDateTime createdAt;
}
