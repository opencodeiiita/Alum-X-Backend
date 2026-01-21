package com.opencode.alumxbackend.jobposts.service;

import com.opencode.alumxbackend.jobposts.dto.*;
import com.opencode.alumxbackend.jobposts.model.JobPost;

import java.util.List;

public interface JobPostService {
    JobPost createJobPost(JobPostRequest request);
    void deletePostByUser(Long userId, Long postId);
    List<JobPostResponse> getPostsByUser(Long userId);
    CommentResponse addComment(Long jobPostId, CommentRequest request);
    void likePost(Long postId, Long userId);
    PagedPostResponse searchPosts(PostSearchRequest searchRequest);
    List<CommentResponse> getCommentsByJobPostId(Long jobPostId);
}
