package com.opencode.alumxbackend.chat.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatSummaryView {
    private final Long chatId;
    private final Long user1Id;
    private final String user1Username;
    private final Long user2Id;
    private final String user2Username;
    private final String lastMessageContent;
    private final Long lastMessageSenderId;
    private final String lastMessageSenderUsername;
    private final LocalDateTime lastMessageCreatedAt;
    private final LocalDateTime chatCreatedAt;
}
