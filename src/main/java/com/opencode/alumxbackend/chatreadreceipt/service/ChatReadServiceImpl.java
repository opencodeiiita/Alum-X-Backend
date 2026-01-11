package com.opencode.alumxbackend.chatreadreceipt.service;

import com.opencode.alumxbackend.chat.model.Chat;
import com.opencode.alumxbackend.chat.repository.ChatRepository;
import com.opencode.alumxbackend.chatreadreceipt.dto.ChatReadResponse;
import com.opencode.alumxbackend.chatreadreceipt.dto.UnreadCountResponse;
import com.opencode.alumxbackend.chatreadreceipt.model.ChatReadState;
import com.opencode.alumxbackend.chatreadreceipt.repository.ChatReadStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatReadServiceImpl implements ChatReadService {

    private final ChatReadStateRepository chatReadStateRepository;
    private final ChatRepository chatRepository;

    @Override
    public ChatReadResponse updateLastRead(Long chatId, Long userId, Long lastReadMessageId) {
        ChatReadState state = chatReadStateRepository.findByChatIdAndUserId(chatId, userId)
                .orElse(ChatReadState.builder()
                        .chatId(chatId)
                        .userId(userId)
                        .lastReadMessageId(lastReadMessageId)
                        .build());

        if (state.getLastReadMessageId() == null || lastReadMessageId > state.getLastReadMessageId()) {
            state.setLastReadMessageId(lastReadMessageId);
            chatReadStateRepository.save(state);
        }

        return new ChatReadResponse(state.getUserId(), state.getLastReadMessageId());
    }

    @Override
    @Transactional(readOnly = true)
    public ChatReadResponse getLastReadMessage(Long chatId, Long userId) {
        return chatReadStateRepository.findByChatIdAndUserId(chatId, userId)
                .map(state -> new ChatReadResponse(state.getUserId(), state.getLastReadMessageId()))
                .orElse(new ChatReadResponse(userId, null));
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long chatId, Long userId) {
        Long lastReadMessageId = chatReadStateRepository.findByChatIdAndUserId(chatId, userId)
                .map(ChatReadState::getLastReadMessageId)
                .orElse(null);

        Long unreadCount;
        if (lastReadMessageId == null) {
            unreadCount = chatReadStateRepository.countAllMessagesFromOther(chatId, userId);
        } else {
            unreadCount = chatReadStateRepository.countUnreadMessages(chatId, userId, lastReadMessageId);
        }

        return new UnreadCountResponse(chatId, userId, unreadCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnreadCountResponse> getAllUnreadCounts(Long userId) {
        List<Chat> userChats = chatRepository.findAll().stream()
                .filter(chat -> chat.getUser1Id().equals(userId) || chat.getUser2Id().equals(userId))
                .collect(Collectors.toList());

        Map<Long, Long> readStates = chatReadStateRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(ChatReadState::getChatId, ChatReadState::getLastReadMessageId));

        List<UnreadCountResponse> results = new ArrayList<>();
        for (Chat chat : userChats) {
            Long lastReadMessageId = readStates.get(chat.getChatID());
            Long unreadCount;

            if (lastReadMessageId == null) {
                unreadCount = chatReadStateRepository.countAllMessagesFromOther(chat.getChatID(), userId);
            } else {
                unreadCount = chatReadStateRepository.countUnreadMessages(chat.getChatID(), userId, lastReadMessageId);
            }

            results.add(new UnreadCountResponse(chat.getChatID(), userId, unreadCount));
        }

        return results;
    }
}
