package com.opencode.alumxbackend.chat.controller;

import java.util.logging.Logger;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;

import com.opencode.alumxbackend.auth.security.UserPrincipal;
import com.opencode.alumxbackend.chat.dto.ChatSendRequest;
import com.opencode.alumxbackend.chat.dto.ChatSendResponse;
import com.opencode.alumxbackend.chat.dto.ChatSummaryResponse;
import com.opencode.alumxbackend.chat.service.ChatService;
import com.opencode.alumxbackend.common.exception.Errors.UnauthorizedAccessException;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private static final Logger logger = Logger.getLogger(ChatController.class.getName());
    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatSummaryResponse>> listUserChats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal user)) {
            throw new UnauthorizedAccessException("User must be authenticated to view chats");
        }

        List<ChatSummaryResponse> chats = chatService.listUserChats(user.getId());
        return ResponseEntity.ok(chats);
    }

    @PostMapping("/send")
    public ResponseEntity<ChatSendResponse> sendMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChatSendRequest request) {


        logger.info("[New Message] User " + userPrincipal.getId() + " (" + userPrincipal.getEmail() + ") " +
                "- " + request.getSenderId() + " sent a message to " + request.getRecieverId() +
                ": " + request.getContent());


        if (!request.getSenderId().equals(userPrincipal.getId())) {
            logger.warning("User " + userPrincipal.getId() + " attempted to send message as user " + request.getSenderId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }

        ChatSendResponse response = chatService.createMessage(
                request.getSenderId(),
                request.getRecieverId(),
                request.getContent()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
