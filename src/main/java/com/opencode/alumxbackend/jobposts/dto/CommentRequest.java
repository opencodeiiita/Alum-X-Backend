package com.opencode.alumxbackend.jobposts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "Comment content cannot be empty")
        @Size(max = 500 , message = "Comment must be under 500 characters")
        String content,
        Long userId

) {
}
