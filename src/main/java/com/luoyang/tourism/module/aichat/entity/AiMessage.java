package com.luoyang.tourism.module.aichat.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiMessage {
    private Long id;
    private Long conversationId;
    private Long userId;
    private Integer sequenceNo;
    private String role;
    private String content;
    private Integer status;
    private String clientMessageId;
    private String traceId;
    private String modelName;
    private String tokenUsage;
    private Integer latencyMs;
    private Integer grounded;
    private LocalDateTime createdAt;
    private Integer isDeleted;
}
