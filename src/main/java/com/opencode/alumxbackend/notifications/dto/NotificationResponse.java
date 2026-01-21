package com.opencode.alumxbackend.notifications.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private String type;
    private String message;
    private Long referenceId;
    private LocalDateTime createdAt;
}
