package com.luoyang.tourism.module.system.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * SystemConfig 数据库实体。
 * <p>字段与数据表 system_config 对应，不直接作为接口响应。</p>
 */
@Data
public class SystemConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String valueType;
    private String description;
    private Integer isSensitive;
    private Integer status;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer isDeleted;
}
