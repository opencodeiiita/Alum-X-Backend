package com.opencode.alumxbackend.jobposts.dto;

import com.opencode.alumxbackend.jobposts.model.JobPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobPostResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static JobPostResponse fromEntity(JobPost jobPost) {
        return JobPostResponse.builder()
                .id(jobPost.getPostId())
                .title(jobPost.getUsername() + "'s Job Post")
                .content(jobPost.getDescription())
                .createdAt(jobPost.getCreatedAt())
                .updatedAt(jobPost.getCreatedAt())
                .build();
    }

    public static List<JobPostResponse> fromEntities(List<JobPost> jobPosts) {
        return jobPosts.stream()
                .map(JobPostResponse::fromEntity)
                .toList();
    }
}
