package com.opencode.alumxbackend.connection.service;

import java.util.List;

import com.opencode.alumxbackend.connection.model.Connection;

public interface ConnectionService {
    
    void sendConnectionRequest(Long senderId, Long receiverId);

    void acceptConnectionRequest(Long connectionId, Long userId);

    void rejectConnectionRequest(Long connectionId, Long userId);

    void cancelConnectionRequest(Long connectionId, Long userId);

    List<Connection> getPendingReceivedRequests(Long userId);

    List<Connection> getPendingSentRequests(Long userId);

    List<Connection> getAcceptedConnections(Long userId);
}
