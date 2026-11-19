package com.luoyang.tourism.module.content.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * GuideDay 数据库实体。
 * <p>字段与数据表 guide_day 对应，不直接作为接口响应。</p>
 */
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
