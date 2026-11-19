package com.luoyang.tourism.module.appuser.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserFavorite {
    private Long id;
    private Long userId;
    private Integer bizType;
    private Long bizId;
    private LocalDateTime createdAt;
}
