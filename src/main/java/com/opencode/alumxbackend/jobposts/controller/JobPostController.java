package com.opencode.alumxbackend.jobposts.controller;

import com.opencode.alumxbackend.common.exception.UnauthorizedAccessException;
import com.opencode.alumxbackend.jobposts.dto.JobPostRequest;
import com.opencode.alumxbackend.jobposts.model.JobPost;
import com.opencode.alumxbackend.jobposts.service.JobPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/job-posts")
@RequiredArgsConstructor
public class JobPostController {
    private final JobPostService jobPostService;
    private static final String DUMMY_TOKEN = "alumx-dev-token";
    private static final Logger logger = Logger.getLogger(JobPostController.class.getName());


    @PostMapping
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
}
