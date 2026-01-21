package com.opencode.alumxbackend.chat.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSummaryResponse {
    private Long chatId;
    private Long otherUserId;
    private String otherUsername;
    private String lastMessageContent;
    private Long lastMessageSenderId;
    private String lastMessageSenderUsername;
    private LocalDateTime lastMessageAt;
}
