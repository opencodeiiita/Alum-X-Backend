package com.opencode.alumxbackend.connection.service;

import com.opencode.alumxbackend.connection.model.Connection;
import com.opencode.alumxbackend.connection.model.ConnectionStatus;
import com.opencode.alumxbackend.connection.repository.ConnectionRepository;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendConnectionRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Users cannot send a connection request to themselves.");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found using ID: " + senderId));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found using ID: " + receiverId));

        Optional<Connection> existingConnection = connectionRepository.findBySenderAndReceiver(sender, receiver);

        if (existingConnection.isPresent()) {
            Connection connection = existingConnection.get();
            if (connection.getStatus() == ConnectionStatus.ACCEPTED) {
                throw new IllegalStateException("User is already connected");
            }
            if (connection.getStatus() == ConnectionStatus.PENDING) {
                throw new IllegalStateException("Connection request already sent");
            }
            // If REJECTED, we might allow re-sending. For now, logic implies new request if
            // not Accepted/Pending.
        }

        // Also check if receiver already sent a request (Reverse direction)
        Optional<Connection> reverseConnection = connectionRepository.findBySenderAndReceiver(receiver, sender);
        if (reverseConnection.isPresent()) {
            Connection connection = reverseConnection.get();
            if (connection.getStatus() == ConnectionStatus.ACCEPTED) {
                throw new IllegalStateException("User is already connected");
            }
            if (connection.getStatus() == ConnectionStatus.PENDING) {
                throw new IllegalStateException("Receiver has already sent you a request. Please accept it instead.");
            }
        }

        Connection newConnection = Connection.builder()
                .sender(sender)
                .receiver(receiver)
                .status(ConnectionStatus.PENDING)
                .build();

        connectionRepository.save(newConnection);
    }
}
