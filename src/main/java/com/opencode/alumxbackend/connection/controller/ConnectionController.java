package com.opencode.alumxbackend.connection.controller;

import com.opencode.alumxbackend.connection.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @PostMapping("/{targetUserId}/connect")
    public ResponseEntity<Map<String, String>> sendConnectionRequest(
            @PathVariable Long targetUserId,
            @RequestHeader("X-USER-ID") Long senderId) {

        try {
            connectionService.sendConnectionRequest(senderId, targetUserId);
            return ResponseEntity.ok(Map.of("message", "Connection request sent successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}
