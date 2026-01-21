package com.opencode.alumxbackend.groupchatmessages.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencode.alumxbackend.groupchat.model.GroupChat;
import com.opencode.alumxbackend.groupchat.model.Participant;
import com.opencode.alumxbackend.groupchat.repository.GroupChatRepository;
import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageResponse;
import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageSearchRequest;
import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageSearchResponse;
import com.opencode.alumxbackend.groupchatmessages.dto.SendGroupMessageRequest;
import com.opencode.alumxbackend.groupchatmessages.exception.GroupNotFoundException;
import com.opencode.alumxbackend.groupchatmessages.exception.InvalidMessageException;
import com.opencode.alumxbackend.groupchatmessages.exception.InvalidRequestException;
import com.opencode.alumxbackend.groupchatmessages.exception.UserNotMemberException;
import com.opencode.alumxbackend.groupchatmessages.model.GroupMessage;
import com.opencode.alumxbackend.groupchatmessages.repository.GroupMessageRepository;
import com.opencode.alumxbackend.users.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupMessageServiceImpl implements GroupMessageService {

    private final UserRepository userRepository;
    private final GroupMessageRepository messageRepository;
    private final GroupChatRepository groupChatRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public GroupMessageResponse sendMessage(
            Long groupId,
            SendGroupMessageRequest request) {

        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group id not found " + groupId));

        boolean isMember = group.getParticipants()
                .stream()
                .anyMatch(p -> p.getUserId().equals(request.getUserId()));

        if (!isMember) {
            throw new UserNotMemberException(request.getUserId());
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new InvalidMessageException("Message cannot be empty");
        }

        Participant sender = group.getParticipants().stream()
                .filter(p -> p.getUserId().equals(request.getUserId()))
                .findFirst()
                .orElseThrow();
        GroupMessage message = GroupMessage.builder()
                .groupId(groupId)
                .senderUserId(sender.getUserId())
                .senderUsername(sender.getUsername())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        messageRepository.save(message);

        // Broadcast message to all subscribers of this group's topic in real-time
        GroupMessageResponse response = mapToResponse(message);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, response);

        return response;
    }

    @Override
    public List<GroupMessageResponse> fetchMessages(
            Long groupId,
            Long userId) {

        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + groupId));

        boolean isMember = group.getParticipants()
                .stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        if (!isMember) {
            throw new UserNotMemberException(userId);
        }

        return messageRepository.findByGroupIdOrderByCreatedAtAsc(groupId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private GroupMessageResponse mapToResponse(GroupMessage message) {
        return GroupMessageResponse.builder()
                .id(message.getId())
                .senderUserId(message.getSenderUserId())
                .senderUsername(message.getSenderUsername())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

    @Override
    public Page<GroupMessageResponse> getGroupMessagesWithPagination(Long groupId, Long userId, int page, int size) {
        // Validate pagination parameters
        if (page < 0) {
            throw new InvalidRequestException("Page index must not be less than zero");
        }
        if (size < 1) {
            throw new InvalidRequestException("Page size must be greater than zero");
        }
        
        // Validate group exists
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + groupId));

        // Check if user is a member of the group
        boolean isMember = group.getParticipants()
                .stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        if (!isMember) {
            throw new UserNotMemberException(userId);
        }

        // Create pageable with sorting by createdAt ascending
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

        // Fetch paginated messages
        Page<GroupMessage> messagePage = messageRepository.findByGroupId(groupId, pageable);

        // Map to response DTOs
        return messagePage.map(this::mapToResponse);
    }

    @Override
    public void deleteMessage(Long groupId, Long messageId, Long userId) {
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        boolean isMember = group.getParticipants()
                .stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        if (!isMember) {
            throw new UserNotMemberException(userId);
        }

        GroupMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new InvalidMessageException("Message not found"));

        if (!message.getGroupId().equals(groupId)) {
            throw new InvalidMessageException("Message does not belong to this group");
        }

        if (!message.getSenderUserId().equals(userId)) {
            throw new InvalidMessageException("You are not the sender of this message");
        }

        messageRepository.delete(message);
    }

    @Override
    public GroupMessageSearchResponse searchForMessage(Long groupId, Long userId, GroupMessageSearchRequest request) {

        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group id not found: " + groupId));

        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        boolean isMember = groupChat.getParticipants()
                .stream()
                .anyMatch(p -> p.getUserId().equals(userId));

        if (!isMember) {
            throw new RuntimeException("User not in group! Access Denied");
        }

        String query = request.getQuery().trim();

        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("createdAt").ascending());

        Page<GroupMessage> resultPage = messageRepository.findByGroupIdAndContentContainingIgnoreCase(groupId, query, pageable);

        List<GroupMessageResponse> messages = resultPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return GroupMessageSearchResponse.builder()
                .groupId(groupId)
                .messages(messages)
                .totalMatches(resultPage.getTotalElements())
                .page(resultPage.getNumber())
                .totalPages(resultPage.getTotalPages())
                .build();

    }
}
