package com.opencode.alumxbackend.chatreadreceipt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_read_states",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_user", columnNames = {"chat_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_chat_read_chat_id", columnList = "chat_id"),
                @Index(name = "idx_chat_read_user_id", columnList = "user_id")
        })
@Builder
public class ChatReadState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;
}
