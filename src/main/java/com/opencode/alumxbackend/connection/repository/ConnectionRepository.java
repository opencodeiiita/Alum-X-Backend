package com.opencode.alumxbackend.connection.repository;

import com.opencode.alumxbackend.connection.model.Connection;
import com.opencode.alumxbackend.connection.model.ConnectionStatus;
import com.opencode.alumxbackend.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, ConnectionStatus status);

    Optional<Connection> findBySenderAndReceiver(User sender, User receiver);
}
