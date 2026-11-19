package com.luoyang.tourism.module.aichat.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.aichat.pojo.dto.AiChatDtos;
import com.luoyang.tourism.module.aichat.pojo.vo.AiChatVos;
import java.util.List;

/**
 * AiChatService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */
public interface AiChatService {
    /** 执行 createConversation 业务操作。 */
    public AiChatVos.ConversationVO createConversation(Long userId, AiChatDtos.CreateConversationRequest request);

    /** 执行 listConversations 业务操作。 */
    public PageResult<AiChatVos.ConversationItemVO> listConversations(Long userId, Integer page, Integer pageSize);

    /** 查询会话消息。 */
    public AiChatVos.MessagePageVO messages(Long userId, String threadId, Integer cursor, Integer limit);

    /** 执行 deleteConversation 业务操作。 */
    public void deleteConversation(Long userId, String threadId);

    /** 执行一次 AI 对话。 */
    public AiChatVos.ChatVO chat(Long userId, AiChatDtos.ChatRequest request, String idempotencyKey);

    /** 执行 recentMessages 业务操作。 */
    public List<AiChatVos.MessageVO> recentMessages(String threadId, Integer limit);
}
