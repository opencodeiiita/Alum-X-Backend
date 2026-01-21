package com.opencode.alumxbackend.users.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opencode.alumxbackend.users.dto.ColorAwareAuraResponse;
import com.opencode.alumxbackend.users.dto.UserAuraResponse;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAuraServiceImpl implements UserAuraService {

    private final UserRepository userRepository;
    private final AuraColorService auraColorService;

    @Override
    public UserAuraResponse getAuraResponse(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserAuraResponse.builder()
                .skills(nullSafe(user.getSkills()))
                .education(nullSafe(user.getEducation()))
                .techStack(nullSafe(user.getTechStack()))
                .languages(nullSafe(user.getLanguages()))
                .frameworks(nullSafe(user.getFrameworks()))
                .communicationSkills(nullSafe(user.getCommunicationSkills()))
                .certifications(nullSafe(user.getCertifications()))
                .projects(nullSafe(user.getProjects()))
                .softSkills(nullSafe(user.getSoftSkills()))
                .hobbies(nullSafe(user.getHobbies()))
                .experience(nullSafe(user.getExperience()))
                .internships(nullSafe(user.getInternships()))
                .build();
    }

    @Override
    public ColorAwareAuraResponse getColorAwareAuraResponse(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ColorAwareAuraResponse.builder()
                .skills(auraColorService.mapToColorAwareElements("skills", user.getSkills()))
                .education(auraColorService.mapToColorAwareElements("education", user.getEducation()))
                .techStack(auraColorService.mapToColorAwareElements("techStack", user.getTechStack()))
                .languages(auraColorService.mapToColorAwareElements("languages", user.getLanguages()))
                .frameworks(auraColorService.mapToColorAwareElements("frameworks", user.getFrameworks()))
                .communicationSkills(auraColorService.mapToColorAwareElements("softSkills", user.getCommunicationSkills()))
                .certifications(auraColorService.mapToColorAwareElements("education", user.getCertifications()))
                .projects(auraColorService.mapToColorAwareElements("skills", user.getProjects()))
                .softSkills(auraColorService.mapToColorAwareElements("softSkills", user.getSoftSkills()))
                .hobbies(auraColorService.mapToColorAwareElements("softSkills", user.getHobbies()))
                .experience(auraColorService.mapToColorAwareElements("experience", user.getExperience()))
                .internships(auraColorService.mapToColorAwareElements("experience", user.getInternships()))
                .build();
    }

    private <T> List<T> nullSafe(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
