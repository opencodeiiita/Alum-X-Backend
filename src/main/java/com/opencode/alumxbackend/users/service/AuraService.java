package com.opencode.alumxbackend.users.service;

import com.opencode.alumxbackend.users.dto.AuraResponse;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AuraService {

    private final UserRepository userRepository;

    public AuraService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuraResponse getUserAura(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("user not found"));

        return AuraResponse.builder()
                .skills(user.getSkills() != null ? user.getSkills() : new ArrayList<>())
                .education(user.getEducation() != null ? user.getEducation() : new ArrayList<>())
                .techStack(user.getTechStack() != null ? user.getTechStack() : new ArrayList<>())
                .languages(user.getLanguages() != null ? user.getLanguages() : new ArrayList<>())
                .frameworks(user.getFrameworks() != null ? user.getFrameworks() : new ArrayList<>())
                .communicationSkills(user.getCommunicationSkills() != null ? user.getCommunicationSkills() : new ArrayList<>())
                .certifications(user.getCertifications() != null ? user.getCertifications() : new ArrayList<>())
                .projects(user.getProjects() != null ? user.getProjects() : new ArrayList<>())
                .softSkills(user.getSoftSkills() != null ? user.getSoftSkills() : new ArrayList<>())
                .hobbies(user.getHobbies() != null ? user.getHobbies() : new ArrayList<>())
                .experience(user.getExperience() != null ? user.getExperience() : new ArrayList<>())
                .internships(user.getInternships() != null ? user.getInternships() : new ArrayList<>())
                .build();


    }
}
