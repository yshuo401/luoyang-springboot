package com.luoyang.tourism.module.aichat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AiChatDtos {
    private AiChatDtos() {
    }

    public record CreateConversationRequest(@Size(max = 100) String title) {
    }

    public record ChatRequest(
            @NotBlank
            @Pattern(regexp = "^[0-9A-HJKMNP-TV-Z]{26}$", message = "conversationId 必须是 ULID")
            String conversationId,
            @NotBlank
            @Pattern(regexp = "^[A-Za-z0-9-]{8,36}$", message = "clientMessageId 格式不正确")
            String clientMessageId,
            @NotBlank @Size(min = 1, max = 2000) String message
    ) {
    }
}
