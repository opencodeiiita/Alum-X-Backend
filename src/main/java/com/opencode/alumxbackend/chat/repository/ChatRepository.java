package com.opencode.alumxbackend.chat.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.opencode.alumxbackend.chat.model.Chat;
import com.opencode.alumxbackend.chat.dto.ChatSummaryView;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    Optional<Chat> findByUser1IdAndUser2Id(
            Long user1Id,
            Long user2Id
    );

    @Query("""
        select new com.opencode.alumxbackend.chat.dto.ChatSummaryView(
            c.chatID,
            c.user1Id,
            c.user1Username,
            c.user2Id,
            c.user2Username,
            m.content,
            m.senderId,
            m.senderUsername,
            m.createdAt,
            c.createdAt
        )
        from Chat c
        left join Message m on m.messageID = (
            select max(m2.messageID) from Message m2 where m2.chat = c
        )
        where c.user1Id = :userId or c.user2Id = :userId
        order by coalesce(m.createdAt, c.createdAt) desc
        """)
    List<ChatSummaryView> findChatSummariesForUser(Long userId);
}
