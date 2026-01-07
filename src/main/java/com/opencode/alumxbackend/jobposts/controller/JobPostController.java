package com.opencode.alumxbackend.jobposts.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.opencode.alumxbackend.common.exception.Errors.UnauthorizedAccessException;
import com.opencode.alumxbackend.jobposts.dto.JobPostRequest;
import com.opencode.alumxbackend.jobposts.dto.JobPostResponse;
import com.opencode.alumxbackend.jobposts.dto.PagedPostResponse;
import com.opencode.alumxbackend.jobposts.dto.PostSearchRequest;
import com.opencode.alumxbackend.jobposts.model.JobPost;
import com.opencode.alumxbackend.jobposts.service.JobPostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobPostController {
    private final JobPostService jobPostService;
    private static final String DUMMY_TOKEN = "alumx-dev-token";

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<List<JobPostResponse>> getPostsByUser(@PathVariable Long userId) {
        List<JobPostResponse> posts = jobPostService.getPostsByUser(userId);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/job-posts")
    public ResponseEntity<?> createJobPost(
            @RequestHeader(value = "X-DUMMY-TOKEN", required = false) String token,
            @Valid @RequestBody JobPostRequest request
    ) {
        if (token == null || !token.equals(DUMMY_TOKEN)) {
            throw new UnauthorizedAccessException("Unauthorized: Invalid or missing X-DUMMY-TOKEN header");
        }

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
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        jobPostService.likePost(postId, userId);
        return ResponseEntity.ok(Map.of("message", "Post liked successfully"));
    }

    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<?> deleteJobPost(
            @RequestHeader(value = "X-DUMMY-TOKEN", required = false) String token,
            @PathVariable Long jobId,
            @RequestParam Long userId
    ) {
        if (token == null || !token.equals(DUMMY_TOKEN)) {
            throw new UnauthorizedAccessException("Unauthorized: Invalid or missing X-DUMMY-TOKEN header");
        }

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
            @RequestHeader(value = "X-DUMMY-TOKEN", required = false) String token,
            @Valid @RequestBody JobPostRequest request
    ) {
        if (token == null || !token.equals(DUMMY_TOKEN)) {
            throw new UnauthorizedAccessException("Unauthorized: Invalid or missing X-DUMMY-TOKEN header");
        }

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
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        jobPostService.likePost(postId, userId);
        return ResponseEntity.ok(Map.of("message", "Post liked successfully"));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @RequestHeader(value = "X-DUMMY-TOKEN", required = false) String token,
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        if (token == null || !token.equals(DUMMY_TOKEN)) {
            throw new UnauthorizedAccessException("Unauthorized: Invalid or missing X-DUMMY-TOKEN header");
        }

        jobPostService.deletePostByUser(userId, postId);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }
}
