package com.opencode.alumxbackend.jobposts.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JobPostRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Job description is required")
    private String description;

    private List<String> imageUrls;

}


