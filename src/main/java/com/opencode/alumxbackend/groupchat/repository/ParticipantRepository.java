package com.opencode.alumxbackend.groupchat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opencode.alumxbackend.groupchat.model.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByGroupChat_GroupIdAndUserId(Long groupId, Long userId);
    boolean existsByGroupChat_GroupIdAndUserId(Long groupId, Long userId);
}
