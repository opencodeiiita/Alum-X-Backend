package com.opencode.alumxbackend.users.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class AuraResponse {
    private List<String> skills;
    private List<String> education;
    private List<String> techStack;
    private List<String> languages;
    private List<String> frameworks;
    private List<String> communicationSkills;
    private List<String> certifications;
    private List<String> projects;
    private List<String> softSkills;
    private List<String> hobbies;
    private List<String> experience;
    private List<String> internships;


}
