package com.opencode.alumxbackend.resume.repository;

import com.opencode.alumxbackend.resume.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume,Long> {
    Optional<Resume> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
