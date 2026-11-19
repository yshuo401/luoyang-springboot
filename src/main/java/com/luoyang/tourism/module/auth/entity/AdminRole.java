package com.luoyang.tourism.module.auth.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminRole {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer version;
    private Integer isDeleted;
}
