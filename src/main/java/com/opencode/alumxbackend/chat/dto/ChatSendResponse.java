package com.opencode.alumxbackend.chat.dto;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSendResponse {

    private Long messageId;
    private Long chatId;
    private String senderUsername;
    private String receiverUsername;
    private String content;
    private LocalDateTime createdAt;
}
