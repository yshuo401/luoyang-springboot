package com.luoyang.tourism.module.content.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScenicSpot {
    private Long id;
    private Long categoryId;
    private String name;
    private String subtitle;
    private String summary;
    private Long coverFileId;
    private String contentHtml;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String openTimeDesc;
    private String ticketInfo;
    private Integer playDurationMinutes;
    private String phone;
    private String trafficInfo;
    private String tips;
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
    private String categoryName;
}
