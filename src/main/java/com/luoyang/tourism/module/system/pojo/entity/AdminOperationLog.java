package com.luoyang.tourism.module.system.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * AdminOperationLog 数据库实体。
 * <p>字段与数据表 admin_operation_log 对应，不直接作为接口响应。</p>
 */
@Data
public class AdminOperationLog {
    private Long id;
    private Long adminUserId;
    private String usernameSnapshot;
    private String module;
    private String operationType;
    private String httpMethod;
    private String requestUri;
    private String requestParams;
    private String responseCode;
    private Integer success;
    private String ip;
    private String userAgent;
    private String traceId;
    private Integer durationMs;
    private LocalDateTime createdAt;
}
