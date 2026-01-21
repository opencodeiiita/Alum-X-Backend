package com.opencode.alumxbackend.groupchat.repository;

import com.opencode.alumxbackend.groupchat.model.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {

    @Query("SELECT g FROM GroupChat g JOIN g.participants p WHERE p.userId = :userId")
    List<GroupChat> findGroupsByUserId(@Param("userId") Long userId);
}
