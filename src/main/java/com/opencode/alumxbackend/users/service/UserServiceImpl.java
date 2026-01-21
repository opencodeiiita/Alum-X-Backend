package com.opencode.alumxbackend.users.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.opencode.alumxbackend.common.exception.Errors.BadRequestException;
import com.opencode.alumxbackend.users.dto.UserProfileResponse;
import com.opencode.alumxbackend.users.dto.UserProfileUpdateRequest;
import com.opencode.alumxbackend.users.dto.UserRequest;
import com.opencode.alumxbackend.users.dto.UserResponseDto;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(UserRequest request) {

        // 1️⃣ Check uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists: " + request.getUsername());
        }

        // 2️⃣ Validate role
        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role. Must be STUDENT, ALUMNI, or PROFESSOR.");
        }

        // 3️⃣ Optional: validate email format, password length etc.
        if (!request.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(\\.[A-Za-z]{2,})?$")) {
            throw new BadRequestException("Invalid email format: " + request.getEmail());
        }

        if (request.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        // 4️⃣ Create and save user
        User user = User.builder()
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .profileCompleted(true) // default for dev
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfileResponse getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return mapToProfileDTO(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private UserResponseDto mapToResponseDTO(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
    private UserProfileResponse mapToProfileDTO(User user) {
        return UserProfileResponse.builder()
                // Identity
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())

                // Professional summary
                .about(user.getAbout())
                .currentCompany(user.getCurrentCompany())
                .currentRole(user.getCurrentRole())
                .location(user.getLocation())

                // Links
                .linkedinUrl(user.getLinkedinUrl())
                .githubUrl(user.getGithubUrl())
                .portfolioUrl(user.getPortfolioUrl())

                // Skills & background
                .skills(copy(user.getSkills()))
                .education(copy(user.getEducation()))
                .techStack(copy(user.getTechStack()))
                .frameworks(copy(user.getFrameworks()))
                .languages(copy(user.getLanguages()))
                .communicationSkills(copy(user.getCommunicationSkills()))
                .softSkills(copy(user.getSoftSkills()))

                // Experience
                .experience(copy(user.getExperience()))
                .internships(copy(user.getInternships()))
                .projects(copy(user.getProjects()))
                .certifications(copy(user.getCertifications()))

                // Personal
                .hobbies(copy(user.getHobbies()))

                // Status
                .profileCompleted(user.isProfileCompleted())
                .build();
    }


    private List<String> copy(List<String> list) {
        return list == null ? List.of() : List.copyOf(list);
    }



    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Basic
        if (request.getName() != null)
            user.setName(request.getName());

        // Professional summary
        if (request.getAbout() != null)
            user.setAbout(request.getAbout());

        if (request.getCurrentCompany() != null)
            user.setCurrentCompany(request.getCurrentCompany());

        if (request.getCurrentRole() != null)
            user.setCurrentRole(request.getCurrentRole());

        if (request.getLocation() != null)
            user.setLocation(request.getLocation());

        // Links
        if (request.getLinkedinUrl() != null)
            user.setLinkedinUrl(request.getLinkedinUrl());

        if (request.getGithubUrl() != null)
            user.setGithubUrl(request.getGithubUrl());

        if (request.getPortfolioUrl() != null)
            user.setPortfolioUrl(request.getPortfolioUrl());

        // Skills & background
        if (request.getSkills() != null)
            user.setSkills(request.getSkills());

        if (request.getEducation() != null)
            user.setEducation(request.getEducation());

        if (request.getTechStack() != null)
            user.setTechStack(request.getTechStack());

        if (request.getFrameworks() != null)
            user.setFrameworks(request.getFrameworks());

        if (request.getLanguages() != null)
            user.setLanguages(request.getLanguages());

        if (request.getCommunicationSkills() != null)
            user.setCommunicationSkills(request.getCommunicationSkills());

        if (request.getSoftSkills() != null)
            user.setSoftSkills(request.getSoftSkills());

        // Experience
        if (request.getExperience() != null)
            user.setExperience(request.getExperience());

        if (request.getInternships() != null)
            user.setInternships(request.getInternships());

        if (request.getProjects() != null)
            user.setProjects(request.getProjects());

        if (request.getCertifications() != null)
            user.setCertifications(request.getCertifications());

        // Personal
        if (request.getHobbies() != null)
            user.setHobbies(request.getHobbies());

        User updatedUser = userRepository.save(user);
        return mapToProfileDTO(updatedUser);
    }
}
