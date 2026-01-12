package com.opencode.alumxbackend.chatreadreceipt.service;

import com.opencode.alumxbackend.chatreadreceipt.dto.ChatReadResponse;
import com.opencode.alumxbackend.chatreadreceipt.dto.UnreadCountResponse;

import java.util.List;

public interface ChatReadService {

    ChatReadResponse updateLastRead(Long chatId, Long userId, Long lastReadMessageId);

    ChatReadResponse getLastReadMessage(Long chatId, Long userId);

    UnreadCountResponse getUnreadCount(Long chatId, Long userId);

    List<UnreadCountResponse> getAllUnreadCounts(Long userId);
}
