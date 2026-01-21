package com.opencode.alumxbackend.groupchatmessages.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendGroupMessageRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 1000, message = "Message must be at most 1000 characters")
    private String content;
}
