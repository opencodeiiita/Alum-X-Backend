package com.opencode.alumxbackend.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time group chat messaging.
 * 
 * Configuration overview:
 * - Clients connect via: /ws
 * - Clients subscribe to: /topic/group/{groupId}
 * - Messages sent from server to: /topic/group/{groupId}
 * 
 * Flow:
 * 1. Client sends message via REST API (POST /api/groups/{groupId}/messages)
 * 2. Message is validated and saved to database
 * 3. Server broadcasts message to /topic/group/{groupId} via WebSocket
 * 4. All clients subscribed to that topic receive the message instantly
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker to carry messages back to the client
        // on destinations prefixed with "/topic"
        config.enableSimpleBroker("/topic");
        
        // Prefix for messages that are bound for @MessageMapping-annotated methods
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint for WebSocket connections
        // Clients will connect to ws://host:port/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Configure CORS as needed for production
                .withSockJS(); // Fallback for browsers that don't support WebSocket
    }
}
