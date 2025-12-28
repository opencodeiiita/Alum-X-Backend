package com.opencode.alumxbackend.users.dto;

import java.util.List;

public record UserProfileDTO(
        Long id,
        String username,
        String name,
        String email,
        List<String> skills,
        List<String> education,
        List<String> techStack
) {}
