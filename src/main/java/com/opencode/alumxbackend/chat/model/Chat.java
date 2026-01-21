package com.opencode.alumxbackend.chat.model;


import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


//conversation between two users
@Entity
@Table(name = "chats",
        indexes = {
                @Index(name = "idx_user1_id", columnList = "user1_id"),
                @Index(name = "idx_user2_id", columnList = "user2_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_users", columnNames = {"user1_id", "user2_id"})
        }
        
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long chatID;
    
    @Column(name = "user1_id", nullable = false)
    private Long user1Id;

    @Column(name = "user2_id", nullable = false)
    private Long user2Id;

    // optional denormalized fields (fast reads, UI)
    @Column(name = "user1_username", nullable = false)
    private String user1Username;

    @Column(name = "user2_username", nullable = false)
    private String user2Username;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
    }
}
