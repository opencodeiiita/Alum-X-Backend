package com.opencode.alumxbackend.jobposts.repository;

import com.opencode.alumxbackend.jobposts.model.JobPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    List<JobPost> findByUsernameOrderByCreatedAtDesc(String username);
    
    @Query("SELECT p FROM JobPost p WHERE " +
           "(:keyword IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:dateFrom IS NULL OR p.createdAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR p.createdAt <= :dateTo) " +
           "ORDER BY p.createdAt DESC")
    Page<JobPost> searchPosts(
            @Param("keyword") String keyword,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );
}
