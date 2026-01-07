package com.opencode.alumxbackend.jobposts.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "job_posts")
public class JobPost {
    @Id
    @Column(name = "post_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private String username;
    
    @Column(length = 5000)
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "job_post_images", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    @OneToMany(mappedBy = "jobPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<JobPostComment> comments;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
