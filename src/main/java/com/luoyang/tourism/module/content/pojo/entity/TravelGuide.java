package com.luoyang.tourism.module.content.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * TravelGuide 数据库实体。
 * <p>字段与数据表 travel_guide 对应，不直接作为接口响应。</p>
 */
@Data
public class TravelGuide {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private Long coverFileId;
    private String authorName;
    private String contentHtml;
    private Integer travelDays;
    private Integer difficulty;
    private String suitableSeason;
    private Integer heatScore;
    private Integer recommendationWeight;
    private Integer status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer version;
    private Integer isDeleted;
}
