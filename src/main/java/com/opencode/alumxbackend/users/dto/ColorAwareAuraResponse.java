package com.opencode.alumxbackend.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorAwareAuraResponse {
    private List<AuraElement> skills;
    private List<AuraElement> education;
    private List<AuraElement> techStack;
    private List<AuraElement> languages;
    private List<AuraElement> frameworks;
    private List<AuraElement> communicationSkills;
    private List<AuraElement> certifications;
    private List<AuraElement> projects;
    private List<AuraElement> softSkills;
    private List<AuraElement> hobbies;
    private List<AuraElement> experience;
    private List<AuraElement> internships;
}