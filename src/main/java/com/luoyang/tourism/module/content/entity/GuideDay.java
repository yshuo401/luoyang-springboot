package com.luoyang.tourism.module.content.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GuideDay {
    private Long id;
    private Long guideId;
    private Integer dayNo;
    private String title;
    private String summary;
    private String trafficTip;
    private String accommodationTip;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
