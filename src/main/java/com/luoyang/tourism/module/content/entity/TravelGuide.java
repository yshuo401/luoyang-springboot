package com.luoyang.tourism.module.content.entity;

import lombok.Data;
import java.time.LocalDateTime;

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
