package com.opencode.alumxbackend.chatreadreceipt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnreadCountResponse {
    private Long chatId;
    private Long userId;
    private Long unreadCount;
}
