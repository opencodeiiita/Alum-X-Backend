package com.opencode.alumxbackend.chatreadreceipt.repository;

import com.opencode.alumxbackend.chatreadreceipt.model.ChatReadState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatReadStateRepository extends JpaRepository<ChatReadState, Long> {

    Optional<ChatReadState> findByChatIdAndUserId(Long chatId, Long userId);

    List<ChatReadState> findByChatId(Long chatId);

    List<ChatReadState> findByUserId(Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.chatID = :chatId " +
            "AND m.senderId != :userId " +
            "AND m.messageID > :lastReadMessageId")
    Long countUnreadMessages(@Param("chatId") Long chatId,
                             @Param("userId") Long userId,
                             @Param("lastReadMessageId") Long lastReadMessageId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.chatID = :chatId " +
            "AND m.senderId != :userId")
    Long countAllMessagesFromOther(@Param("chatId") Long chatId,
                                   @Param("userId") Long userId);
}
