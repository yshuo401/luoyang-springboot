package com.luoyang.tourism.module.aichat.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * AiMessage 数据库实体。
 * <p>字段与数据表 ai_message 对应，不直接作为接口响应。</p>
 */
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
