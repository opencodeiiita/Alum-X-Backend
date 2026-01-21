package com.opencode.alumxbackend.groupchatmessages.repository;

import com.opencode.alumxbackend.groupchatmessages.model.GroupMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
    List<GroupMessage> findByGroupIdOrderByCreatedAtAsc(Long groupId);

    boolean existsById(Long id);

    Page<GroupMessage> findByGroupIdAndContentContainingIgnoreCase(Long groupId, String content, Pageable pageable);
    
    Page<GroupMessage> findByGroupId(Long groupId, Pageable pageable);
}
