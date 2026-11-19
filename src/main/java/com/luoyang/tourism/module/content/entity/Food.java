package com.luoyang.tourism.module.content.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Food {
    private Long id;
    private Long foodCategoryId;
    private String name;
    private String summary;
    private Long coverFileId;
    private String description;
    private String priceRange;
    private String flavorTags;
    private String recommendShops;
    private String area;
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
