package com.luoyang.tourism.module.aichat.pojo.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * AiConversation 数据库实体。
 * <p>字段与数据表 ai_conversation 对应，不直接作为接口响应。</p>
 */
@Data
public class AiConversation {
    private Long id;
    private String threadId;
    private Long userId;
    private String title;
    private Integer status;
    private LocalDateTime lastMessageAt;
    private Integer messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    private Integer isDeleted;
    private String lastMessagePreview;
}
