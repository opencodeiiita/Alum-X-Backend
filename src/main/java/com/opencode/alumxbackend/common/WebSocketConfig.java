package com.opencode.alumxbackend.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time messaging (group chat and one-to-one chat).
 * 
 * Configuration overview:
 * - Clients connect via: /ws
 * - Clients subscribe to:
 *   - Group chat: /topic/group/{groupId}
 *   - One-to-one chat: /topic/chat/{chatId}
 * - Messages sent from server to:
 *   - Group chat: /topic/group/{groupId}
 *   - One-to-one chat: /topic/chat/{chatId}
 * 
 * Flow:
 * 1. Client sends message via REST API
 * 2. Message is validated and saved to database
 * 3. Server broadcasts message to appropriate topic via WebSocket
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
