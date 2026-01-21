package com.opencode.alumxbackend.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opencode.alumxbackend.chat.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
}
