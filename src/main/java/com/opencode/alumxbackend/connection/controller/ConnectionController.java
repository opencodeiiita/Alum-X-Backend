package com.opencode.alumxbackend.connection.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opencode.alumxbackend.connection.model.Connection;
import com.opencode.alumxbackend.connection.service.ConnectionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @PostMapping("/users/{targetUserId}/connect")
    public ResponseEntity<?> sendConnectionRequest(@PathVariable Long targetUserId, @RequestHeader("X-USER-ID") Long userId) {
        connectionService.sendConnectionRequest(userId, targetUserId);
        return ResponseEntity.ok("Connection request sent");
    }

    @PostMapping("/connections/{connectionId}/accept")
    public ResponseEntity<?> acceptConnectionRequest(@PathVariable Long connectionId, @RequestHeader("X-USER-ID") Long userId) {
        connectionService.acceptConnectionRequest(connectionId, userId);
        return ResponseEntity.ok("Connection request accepted");
    }

    @PostMapping("/connections/{connectionId}/reject")
    public ResponseEntity<?> rejectConnectionRequest(@PathVariable Long connectionId, @RequestHeader("X-USER-ID") Long userId) {
        connectionService.rejectConnectionRequest(connectionId, userId);
        return ResponseEntity.ok("Connection request rejected");
    }

    @PostMapping("/connections/{connectionId}/cancel")
    public ResponseEntity<?> cancelConnectionRequest(@PathVariable Long connectionId, @RequestHeader("X-USER-ID") Long userId) {
        connectionService.cancelConnectionRequest(connectionId, userId);
        return ResponseEntity.ok("Connection request cancelled");
    }

    @GetMapping("/connections/pending/received")
    public ResponseEntity<List<Connection>> getPendingReceivedRequests(@RequestHeader("X-USER-ID") Long userId) {
        return ResponseEntity.ok(connectionService.getPendingReceivedRequests(userId));
    }

    @GetMapping("/connections/pending/sent")
    public ResponseEntity<List<Connection>> getPendingSentRequests(@RequestHeader("X-USER-ID") Long userId) {
        return ResponseEntity.ok(connectionService.getPendingSentRequests(userId));
    }

    @GetMapping("/connections/accepted")
    public ResponseEntity<List<Connection>> getAcceptedConnections(@RequestHeader("X-USER-ID") Long userId) {
        return ResponseEntity.ok(connectionService.getAcceptedConnections(userId));
    }
}
