package com.opencode.alumxbackend.resume.service;

import com.opencode.alumxbackend.common.exception.Errors.InvalidResumeException;
import com.opencode.alumxbackend.common.exception.Errors.ResumeNotFoundException;
import com.opencode.alumxbackend.resume.model.Resume;
import com.opencode.alumxbackend.resume.repository.ResumeRepository;
import com.opencode.alumxbackend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResumeService {
    private final ResumeRepository resumeRepository;

    @Value("${resume.upload.dir}")
    private String uploadDir;

    public void uploadResume(String userId, MultipartFile file) throws Exception {

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new InvalidResumeException("File size must be less than 5MB");
        }

        String contentType = file.getContentType();
        if (!("application/pdf".equals(contentType)
                || "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType))) {
            throw new InvalidResumeException("Only PDF and DOCX files are allowed");
        }

        File directory = new File(uploadDir);
        if (!directory.exists()) directory.mkdirs();

        String filePath = uploadDir + "/" + userId + "_" + file.getOriginalFilename();
        Files.write(new File(filePath).toPath(), file.getBytes());

        Resume resume = Resume.builder()
                .user(User)
                .fileName(file.getOriginalFilename())
                .fileType(contentType)
                .filePath(filePath)
                .uploadedAt(LocalDateTime.now())
                .build();

        resumeRepository.save(resume);
    }

    public Resume getResumeByUserId(String userId) {
        return resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found"));
    }
}
