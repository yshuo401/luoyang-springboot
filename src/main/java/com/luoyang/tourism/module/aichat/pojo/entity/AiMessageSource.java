package com.luoyang.tourism.module.aichat.pojo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AiMessageSource 数据库实体。
 * <p>字段与数据表 ai_message_source 对应，不直接作为接口响应。</p>
 */
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
