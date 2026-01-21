package com.opencode.alumxbackend.chatreadreceipt.controller;

import com.opencode.alumxbackend.chatreadreceipt.dto.ChatReadRequest;
import com.opencode.alumxbackend.chatreadreceipt.dto.ChatReadResponse;
import com.opencode.alumxbackend.chatreadreceipt.dto.UnreadCountResponse;
import com.opencode.alumxbackend.chatreadreceipt.service.ChatReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatReadController {

    private final ChatReadService chatReadService;

    @PostMapping("/{chatId}/read")
    public ResponseEntity<ChatReadResponse> updateRead(
            @PathVariable Long chatId,
            @RequestBody ChatReadRequest request) {
        ChatReadResponse response = chatReadService.updateLastRead(
                chatId, request.getUserId(), request.getLastReadMessageId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{chatId}/last-read/{userId}")
    public ResponseEntity<ChatReadResponse> getLastRead(
            @PathVariable Long chatId,
            @PathVariable Long userId) {
        ChatReadResponse response = chatReadService.getLastReadMessage(chatId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{chatId}/unread-count/{userId}")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @PathVariable Long chatId,
            @PathVariable Long userId) {
        UnreadCountResponse response = chatReadService.getUnreadCount(chatId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-counts/{userId}")
    public ResponseEntity<List<UnreadCountResponse>> getAllUnreadCounts(
            @PathVariable Long userId) {
        List<UnreadCountResponse> response = chatReadService.getAllUnreadCounts(userId);
        return ResponseEntity.ok(response);
    }
}
