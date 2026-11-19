package com.luoyang.tourism.module.aichat.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.aichat.dto.AiChatDtos;
import com.luoyang.tourism.module.aichat.service.AiChatService;
import com.luoyang.tourism.module.aichat.vo.AiChatVos;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app/ai")
@Tag(name = "小程序-AI 会话", description = "会话事实持久化与 FastAPI 内网调用")
@SecurityRequirement(name = "bearerAuth")
public class AiChatController {
    private final AiChatService service;

    @PostMapping("/conversations")
    @Operation(summary = "创建 AI 会话")
    public ApiResponse<AiChatVos.ConversationVO> create(
            @Valid @RequestBody AiChatDtos.CreateConversationRequest body,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.createConversation(AuthClaims.userId(jwt), body));
    }

    @GetMapping("/conversations")
    @Operation(summary = "AI 会话分页列表")
    public ApiResponse<PageResult<AiChatVos.ConversationItemVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.listConversations(AuthClaims.userId(jwt), page, pageSize));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "按游标读取会话消息")
    public ApiResponse<AiChatVos.MessagePageVO> messages(
            @PathVariable String conversationId,
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "20") Integer limit,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.messages(AuthClaims.userId(jwt), conversationId, cursor, limit));
    }

    @DeleteMapping("/conversations/{conversationId}")
    @Operation(summary = "删除会话并清理 checkpoint")
    public ApiResponse<Void> delete(@PathVariable String conversationId,
                                    @AuthenticationPrincipal Jwt jwt) {
        service.deleteConversation(AuthClaims.userId(jwt), conversationId);
        return ApiResponse.success();
    }

    @PostMapping("/chat")
    @Operation(summary = "AI 多轮问答")
    public ApiResponse<AiChatVos.ChatVO> chat(
            @Valid @RequestBody AiChatDtos.ChatRequest body,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.chat(AuthClaims.userId(jwt), body, idempotencyKey));
    }
}
