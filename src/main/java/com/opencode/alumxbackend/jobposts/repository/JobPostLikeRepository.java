package com.opencode.alumxbackend.jobposts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opencode.alumxbackend.jobposts.model.JobPost;
import com.opencode.alumxbackend.jobposts.model.JobPostLike;
import com.opencode.alumxbackend.users.model.User;

@Repository
public interface JobPostLikeRepository extends JpaRepository<JobPostLike,Long> {
    boolean existsByJobPostAndUser(JobPost jobPost, User user);
}
