package com.opencode.alumxbackend.notifications.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.opencode.alumxbackend.common.exception.Errors.BadRequestException;
import com.opencode.alumxbackend.common.exception.Errors.ResourceNotFoundException;
import com.opencode.alumxbackend.notifications.dto.NotificationRequest;
import com.opencode.alumxbackend.notifications.dto.NotificationResponse;
import com.opencode.alumxbackend.notifications.model.Notification;
import com.opencode.alumxbackend.notifications.repository.NotificationRepository;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    public NotificationResponse createNotification(NotificationRequest request) {
        if (request.getUserId() == null ||
            request.getMessage() == null ||
            request.getMessage().isEmpty() ||
            request.getType() == null ||
            request.getType().isEmpty()
        ) {
            throw new BadRequestException("Invalid notification data");
        }
        User user = userRepository.findById(request.getUserId()).orElseThrow(
            () -> new ResourceNotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(request.getType());
        notification.setMessage(request.getMessage());
        notification.setReferenceId(request.getReferenceId());
        notification.setCreatedAt(LocalDateTime.now());

        notification = notificationRepository.save(notification);

        return mapToNotificationResponseDto(notification);
    }

    public List<NotificationResponse> getNotifications (Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
            () -> new ResourceNotFoundException("User not found")
        );
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToNotificationResponseDto)
                .collect(Collectors.toList());
    }

    private NotificationResponse mapToNotificationResponseDto(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .build();
    }

}
