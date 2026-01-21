package com.opencode.alumxbackend.jobposts.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.opencode.alumxbackend.auth.security.UserPrincipal;
import com.opencode.alumxbackend.jobposts.dto.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.opencode.alumxbackend.jobposts.model.JobPost;
import com.opencode.alumxbackend.jobposts.service.JobPostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobPostController {
    private final JobPostService jobPostService;

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<List<JobPostResponse>> getPostsByUser(@PathVariable Long userId) {
        List<JobPostResponse> posts = jobPostService.getPostsByUser(userId);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/job-posts")
    public ResponseEntity<?> createJobPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody JobPostRequest request
    ) {
        JobPost savedPost = jobPostService.createJobPost(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Job post created successfully",
                "postId", savedPost.getPostId(),
                "username", savedPost.getUsername(),
                "createdAt", savedPost.getCreatedAt()
        ));
    }

    @PostMapping("/jobs/{postId}/like")
    public ResponseEntity<?> likePost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        jobPostService.likePost(postId, userId);
        return ResponseEntity.ok(Map.of("message", "Post liked successfully"));
    }

    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<?> deleteJobPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long jobId,
            @RequestParam Long userId
    ) {
        jobPostService.deletePostByUser(userId, jobId);
        return ResponseEntity.ok(Map.of("message", "Job post deleted successfully"));
    }

    @GetMapping("/posts/search")
    public ResponseEntity<PagedPostResponse> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .keyword(keyword)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .page(page)
                .size(size)
                .build();

        PagedPostResponse response = jobPostService.searchPosts(searchRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody JobPostRequest request
    ) {
        JobPost savedPost = jobPostService.createJobPost(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Post created successfully",
                "postId", savedPost.getPostId(),
                "username", savedPost.getUsername(),
                "createdAt", savedPost.getCreatedAt()
        ));
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> likePostNew(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        jobPostService.likePost(postId, userId);
        return ResponseEntity.ok(Map.of("message", "Post liked successfully"));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        jobPostService.deletePostByUser(userId, postId);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    @PostMapping("/jobpost/addcomment/{jobPostId}")
    public ResponseEntity<CommentResponse> addComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long jobPostId,
            @RequestBody CommentRequest request
    ) {
        CommentResponse response = jobPostService.addComment(jobPostId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/jobpost/getcomment/{jobPostId}")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long jobPostId) {
        return ResponseEntity.ok(jobPostService.getCommentsByJobPostId(jobPostId));
    }
}
