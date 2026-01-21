package com.opencode.alumxbackend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSendRequest {
    @NotNull(message = "Sender Id is required")
    private Long senderId;
    
    @NotNull(message = "Reciever Id is required")
    private Long recieverId;

    @NotBlank(message = "Content is required")
    private String content;
}
