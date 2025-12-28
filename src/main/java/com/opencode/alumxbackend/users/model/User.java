package com.opencode.alumxbackend.users.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean profileCompleted;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private List<String> skills;

    @ElementCollection
    @CollectionTable(name = "user_education", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "education")
    private List<String> education;

    @ElementCollection
    @CollectionTable(name = "user_tech_stack", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "tech")
    private List<String> techStack;

    @ElementCollection
    @CollectionTable(name = "user_languages", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "language")
    private List<String> languages;

    @ElementCollection
    @CollectionTable(name = "user_frameworks", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "framework")
    private List<String> frameworks;

    @ElementCollection
    @CollectionTable(name = "user_communication_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "communication_skill")
    private List<String> communicationSkills;

    @ElementCollection
    @CollectionTable(name = "user_certifications", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "certification")
    private List<String> certifications;

    @ElementCollection
    @CollectionTable(name = "user_projects", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "project")
    private List<String> projects;

    @ElementCollection
    @CollectionTable(name = "user_soft_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "soft_skill")
    private List<String> softSkills;

    @ElementCollection
    @CollectionTable(name = "user_hobbies", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "hobby")
    private List<String> hobbies;

    @ElementCollection
    @CollectionTable(name = "user_experience", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "experience")
    private List<String> experience;

    @ElementCollection
    @CollectionTable(name = "user_internships", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "internship")
    private List<String> internships;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
