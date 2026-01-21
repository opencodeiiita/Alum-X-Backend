package com.opencode.alumxbackend.jobposts.dto;



import java.time.LocalDateTime;


public record CommentResponse(
        Long id,
        String content,
        String username,
        LocalDateTime createdAt
) {}
