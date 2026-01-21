package com.opencode.alumxbackend.users.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opencode.alumxbackend.jobposts.service.JobPostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserPostController {
    private final JobPostService jobPostService;

    @DeleteMapping("/{userId}/posts/{postId}")
    public ResponseEntity<?> deletePostByUser(
            @PathVariable Long userId,
            @PathVariable Long postId
    ) {
        jobPostService.deletePostByUser(userId, postId);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }
}
