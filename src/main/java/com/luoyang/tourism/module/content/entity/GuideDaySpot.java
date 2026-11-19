package com.luoyang.tourism.module.content.entity;

import lombok.Data;
import java.time.LocalDateTime;

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
