package com.opencode.alumxbackend.users.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileUpdateRequest {

    // Basic
    private String name;

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
}

