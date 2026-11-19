package com.luoyang.tourism.module.content.pojo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * ScenicSpot 数据库实体。
 * <p>字段与数据表 scenic_spot 对应，不直接作为接口响应。</p>
 */
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
