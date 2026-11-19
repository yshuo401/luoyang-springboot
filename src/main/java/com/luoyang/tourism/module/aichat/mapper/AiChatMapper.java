package com.luoyang.tourism.module.aichat.mapper;

import com.luoyang.tourism.module.aichat.pojo.entity.AiConversation;
import com.luoyang.tourism.module.aichat.pojo.entity.AiMessage;
import com.luoyang.tourism.module.aichat.pojo.entity.AiMessageSource;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AiChatMapper MyBatis 数据访问接口。
 * <p>SQL 只在本层维护，业务层不得拼接用户可控 SQL。</p>
 */
@Mapper
public interface AiChatMapper {
    int insertConversation(AiConversation conversation);
    AiConversation selectConversationByThreadId(@Param("threadId") String threadId);
    AiConversation selectConversationById(@Param("id") Long id);
    List<AiConversation> selectConversations(@Param("userId") Long userId, @Param("offset") long offset,
                                             @Param("limit") int limit);
    long countConversations(@Param("userId") Long userId);
    int addConversationMessages(@Param("id") Long id, @Param("delta") int delta,
                                @Param("lastMessageAt") LocalDateTime lastMessageAt);
    int softDeleteConversation(@Param("id") Long id, @Param("userId") Long userId);

    int nextSequence(@Param("conversationId") Long conversationId);
    int insertMessage(AiMessage message);
    AiMessage selectMessageById(@Param("id") Long id);
    AiMessage selectUserMessageByClientId(@Param("conversationId") Long conversationId,
                                         @Param("clientMessageId") String clientMessageId);
    AiMessage selectAssistantBySequence(@Param("conversationId") Long conversationId,
                                        @Param("sequenceNo") int sequenceNo);
    List<AiMessage> selectMessages(@Param("conversationId") Long conversationId,
                                   @Param("cursor") Integer cursor, @Param("limit") int limit);
    List<AiMessage> selectRecentMessages(@Param("conversationId") Long conversationId,
                                         @Param("limit") int limit);
    int updateAssistantMessage(AiMessage message);
    int resetAssistantForRetry(@Param("id") Long id, @Param("traceId") String traceId);

    int insertSource(AiMessageSource source);
    List<AiMessageSource> selectSourcesByMessageId(@Param("messageId") Long messageId);
    List<AiMessageSource> selectSourcesByMessageIds(@Param("messageIds") List<Long> messageIds);
}
