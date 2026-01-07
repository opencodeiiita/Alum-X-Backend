package com.opencode.alumxbackend.auth.dto;

import com.opencode.alumxbackend.users.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private Long userId;
    private String username;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;
    private String message;
}
