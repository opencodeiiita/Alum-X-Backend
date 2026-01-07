package com.opencode.alumxbackend.groupchatmessages.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageResponse {
    private Long id;
    private Long senderUserId;
    private String senderUsername;
    private String content;
    private LocalDateTime createdAt;
}
