package com.luoyang.tourism.module.content.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * Banner 数据库实体。
 * <p>字段与数据表 banner 对应，不直接作为接口响应。</p>
 */
@Data
public class Banner {
    private Long id;
    private String title;
    private String subtitle;
    private Long imageFileId;
    private Integer linkType;
    private Long targetId;
    private String targetPath;
    private String position;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer version;
    private Integer isDeleted;
}
