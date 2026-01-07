package com.opencode.alumxbackend.jobposts.service;

import com.opencode.alumxbackend.jobposts.dto.CommentRequest;
import com.opencode.alumxbackend.jobposts.dto.JobPostRequest;
import com.opencode.alumxbackend.jobposts.dto.JobPostResponse;
import com.opencode.alumxbackend.jobposts.dto.PagedPostResponse;
import com.opencode.alumxbackend.jobposts.dto.PostSearchRequest;
import com.opencode.alumxbackend.jobposts.model.JobPost;

import java.util.List;

public interface JobPostService {
    JobPost createJobPost(JobPostRequest request);
    void deletePostByUser(Long userId, Long postId);
    List<JobPostResponse> getPostsByUser(Long userId);
    void addComment(Long postId, Long userId, CommentRequest request);
    void likePost(Long postId, Long userId);
    PagedPostResponse searchPosts(PostSearchRequest searchRequest);
}
