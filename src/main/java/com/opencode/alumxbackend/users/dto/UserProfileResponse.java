package com.opencode.alumxbackend.users.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserProfileResponse {
    // Identity (safe to expose)
    private Long id;
    private String username;
    private String name;
    private String email;

    // Professional summary
    private String about;
    private String currentCompany;
    private String currentRole;
    private String location;

    // Links
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;

    // Skills & background
    private List<String> skills;
    private List<String> education;
    private List<String> techStack;
    private List<String> frameworks;
    private List<String> languages;
    private List<String> communicationSkills;
    private List<String> softSkills;

    // Experience
    private List<String> experience;
    private List<String> internships;
    private List<String> projects;
    private List<String> certifications;

    // Personal
    private List<String> hobbies;

    // Profile status
    private Boolean profileCompleted;
}
