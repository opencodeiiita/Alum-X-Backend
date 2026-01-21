package com.opencode.alumxbackend.chat.service;

import com.opencode.alumxbackend.chat.dto.ChatSendResponse;
import com.opencode.alumxbackend.chat.dto.ChatSummaryResponse;
import java.util.List;

public interface ChatService {
    ChatSendResponse createMessage(Long senderId, Long recieverId, String content);

    List<ChatSummaryResponse> listUserChats(Long userId);
}
