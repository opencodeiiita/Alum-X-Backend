package com.opencode.alumxbackend.groupchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupChatRequest {

    @NotBlank(message = "Group name is required")
    private String name;

    @Size(min = 2, message = "At least 2 participants required")
    private List<ParticipantRequest> participants;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParticipantRequest {
        private Long userId;
        private String username;
    }
}
