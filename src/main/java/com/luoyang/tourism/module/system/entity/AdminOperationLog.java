package com.luoyang.tourism.module.system.entity;

import lombok.Data;
import java.time.LocalDateTime;

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
