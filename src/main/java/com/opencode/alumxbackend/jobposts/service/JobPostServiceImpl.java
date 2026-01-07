package com.opencode.alumxbackend.jobposts.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import com.opencode.alumxbackend.jobposts.dto.CommentRequest;
import com.opencode.alumxbackend.jobposts.dto.PagedPostResponse;
import com.opencode.alumxbackend.jobposts.dto.PostSearchRequest;
import com.opencode.alumxbackend.jobposts.model.JobPostComment;
import com.opencode.alumxbackend.jobposts.repository.CommentRepository;
import com.opencode.alumxbackend.jobposts.model.JobPostLike;
import com.opencode.alumxbackend.jobposts.repository.JobPostLikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.opencode.alumxbackend.common.exception.Errors.BadRequestException;
import com.opencode.alumxbackend.common.exception.Errors.ForbiddenException;
import com.opencode.alumxbackend.common.exception.Errors.ResourceNotFoundException;
import com.opencode.alumxbackend.jobposts.dto.JobPostRequest;
import com.opencode.alumxbackend.jobposts.dto.JobPostResponse;
import com.opencode.alumxbackend.jobposts.model.JobPost;
import com.opencode.alumxbackend.jobposts.repository.JobPostRepository;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobPostServiceImpl implements JobPostService {
    private final JobPostLikeRepository jobPostLikeRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    @Override
    public List<JobPostResponse> getPostsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id  not found " + userId));

        List<JobPost> posts = jobPostRepository.findByUsernameOrderByCreatedAtDesc(user.getUsername());
        return JobPostResponse.fromEntities(posts);
    }

    public void addComment(Long postId, Long userId, CommentRequest request) {
        JobPost post = jobPostRepository.findById(postId)
                .orElseThrow(()->new ResourceNotFoundException("job post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobPostComment comment = JobPostComment.builder()
                .jobPost(post)
                .user(user)
                .content(request.content())
                .build();

        commentRepository.save(comment);

    }



    @Override
    public void likePost(Long postId, Long userId) {
        JobPost post = jobPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with postId: " + postId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        try {
            JobPostLike like = JobPostLike.builder()
                    .jobPost(post)
                    .user(user)
                    .build();
            jobPostLikeRepository.save(like);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new BadRequestException("User has already liked this post");
        }
    }

    @Override
    public JobPost createJobPost(JobPostRequest request) {
        if (!userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username does not exist: " + request.getUsername());
        }

        if (request.getDescription().length() > 5000 || request.getDescription().isBlank() || request.getDescription().length() < 50) {
            throw new IllegalArgumentException("Description must be between 50 and 5000 characters");
        }

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            request.getImageUrls().forEach(url -> {
                try {
                    URI.create(url).toURL();
                } catch (IllegalArgumentException | MalformedURLException e) {
                    throw new BadRequestException("Invalid URL: " + url);
                }
            });
        }
        JobPost jobPost = JobPost.builder()
                .username(request.getUsername())
                .description(request.getDescription())
                .imageUrls(request.getImageUrls())
                .createdAt(LocalDateTime.now())
                .build();


        return jobPostRepository.save(jobPost);
    }

    @Override
    public void deletePostByUser(Long userId, Long postId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with the id " + userId));

        JobPost post = jobPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with postId " + postId));

        if (!post.getUsername().equals(user.getUsername())) {
            throw new ForbiddenException("User is not the owner of the post");
        }

        jobPostRepository.delete(post);
    }

    @Override
    public PagedPostResponse searchPosts(PostSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(
                searchRequest.getPageOrDefault(),
                searchRequest.getSizeOrDefault()
        );
        
        Page<JobPost> postPage = jobPostRepository.searchPosts(
                searchRequest.getKeyword(),
                searchRequest.getDateFrom(),
                searchRequest.getDateTo(),
                pageable
        );
        
        Page<JobPostResponse> responsePage = postPage.map(JobPostResponse::fromEntity);
        return PagedPostResponse.fromPage(responsePage);
    }
}
