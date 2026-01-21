package com.opencode.alumxbackend.connection.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencode.alumxbackend.connection.model.Connection;
import com.opencode.alumxbackend.connection.model.ConnectionStatus;
import com.opencode.alumxbackend.connection.repository.ConnectionRepository;
import com.opencode.alumxbackend.users.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionServiceImpl implements ConnectionService {
    
    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    @Override
    public void sendConnectionRequest(Long senderId, Long receiverId) {

        if (!userRepository.existsById(senderId)) {
            throw new EntityNotFoundException("Sender not found");
        }

        if (!userRepository.existsById(receiverId)) {
            throw new EntityNotFoundException("Receiver not found");
        }

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot connect with yourself");
        }

        Optional<Connection> existing = connectionRepository.findByReceiverIdAndSenderId(senderId, receiverId);

        if (existing.isPresent()) {
            if (existing.get().getStatus() == ConnectionStatus.ACCEPTED) {
                throw new IllegalStateException("User is already connected");
            }
            if (existing.get().getStatus() == ConnectionStatus.PENDING) {
                throw new IllegalStateException("Connection request already sent");
            }
        }

        Connection connection = Connection.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .build();

        connectionRepository.save(connection);
    }

    @Override
    public void acceptConnectionRequest(Long connectionId, Long userId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new EntityNotFoundException("Connection not found"));

        if (!connection.getReceiverId().equals(userId)) {
            throw new IllegalStateException("Only the receiver can accept this request");
        }

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("Connection is already " + connection.getStatus().name().toLowerCase());
        }

        connection.setStatus(ConnectionStatus.ACCEPTED);
        connectionRepository.save(connection);
    }

    @Override
    public void rejectConnectionRequest(Long connectionId, Long userId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new EntityNotFoundException("Connection not found"));

        if (!connection.getReceiverId().equals(userId)) {
            throw new IllegalStateException("Only the receiver can reject this request");
        }

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("Connection is already " + connection.getStatus().name().toLowerCase());
        }

        connection.setStatus(ConnectionStatus.REJECTED);
        connectionRepository.save(connection);
    }

    @Override
    public void cancelConnectionRequest(Long connectionId, Long userId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new EntityNotFoundException("Connection not found"));

        if (!connection.getSenderId().equals(userId)) {
            throw new IllegalStateException("Only the sender can cancel this request");
        }

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel connection that is already " + connection.getStatus().name().toLowerCase());
        }

        connectionRepository.delete(connection);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Connection> getPendingReceivedRequests(Long userId) {
        return connectionRepository.findPendingRequestsForUser(userId, ConnectionStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Connection> getPendingSentRequests(Long userId) {
        return connectionRepository.findSentRequestsByUser(userId, ConnectionStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Connection> getAcceptedConnections(Long userId) {
        return connectionRepository.findByUserIdAndStatus(userId, ConnectionStatus.ACCEPTED);
    }
}
