package com.luoyang.tourism.module.aichat.entity;

import lombok.Data;
import java.time.LocalDateTime;

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
