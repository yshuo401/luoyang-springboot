package com.luoyang.tourism.module.aichat.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AiMessageSource {
    private Long id;
    private Long messageId;
    private Integer sourceType;
    private Long sourceId;
    private Long chunkId;
    private String sourceTitle;
    private String sourceUrl;
    private String snippet;
    private BigDecimal score;
    private Integer rankNo;
    private String metadata;
    private LocalDateTime createdAt;
}
