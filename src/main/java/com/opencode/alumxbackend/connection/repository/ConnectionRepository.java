package com.opencode.alumxbackend.connection.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opencode.alumxbackend.connection.model.Connection;
import com.opencode.alumxbackend.connection.model.ConnectionStatus;


public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    Optional<Connection> findByReceiverIdAndSenderId(Long senderId, Long receiverId);

    @Query("SELECT c FROM Connection c WHERE (c.senderId = :userId OR c.receiverId = :userId) AND c.status = :status")
    List<Connection> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ConnectionStatus status);

    @Query("SELECT c FROM Connection c WHERE c.receiverId = :userId AND c.status = :status")
    List<Connection> findPendingRequestsForUser(@Param("userId") Long userId, @Param("status") ConnectionStatus status);

    @Query("SELECT c FROM Connection c WHERE c.senderId = :userId AND c.status = :status")
    List<Connection> findSentRequestsByUser(@Param("userId") Long userId, @Param("status") ConnectionStatus status);
}
