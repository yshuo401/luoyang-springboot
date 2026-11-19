package com.luoyang.tourism.module.content.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * GuideDaySpot 数据库实体。
 * <p>字段与数据表 guide_day_spot 对应，不直接作为接口响应。</p>
 */
@Data
public class GuideDaySpot {
    private Long id;
    private Long guideDayId;
    private Long guideId;
    private Long spotId;
    private Integer sortOrder;
    private Integer stayMinutes;
    private String note;
    private LocalDateTime createdAt;
    private Integer isDeleted;
}
