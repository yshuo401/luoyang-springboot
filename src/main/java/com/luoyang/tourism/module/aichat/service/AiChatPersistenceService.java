package com.luoyang.tourism.module.aichat.service;

import com.luoyang.tourism.integration.ai.AiDtos;
import com.luoyang.tourism.module.aichat.pojo.entity.AiConversation;
import com.luoyang.tourism.module.aichat.pojo.entity.AiMessage;
import com.luoyang.tourism.module.aichat.pojo.entity.AiMessageSource;
import java.util.List;

/**
 * AiChatPersistenceService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */

public interface AiChatPersistenceService {
    /** 执行 startNew 业务操作。 */
    public PreparedMessages startNew(AiConversation conversation, Long userId, String content, String clientMessageId, String traceId);

    /** 执行 retry 业务操作。 */
    public void retry(AiMessage assistant, String traceId);

    /** 执行 complete 业务操作。 */
    public void complete(Long conversationId, AiMessage assistant, AiDtos.ChatResult result, List<AiMessageSource> sources);

    /** 执行 fail 业务操作。 */
    public void fail(Long conversationId, Long assistantId, Integer latencyMs);

    public record PreparedMessages(AiMessage userMessage, AiMessage assistantMessage) {
    }
}
