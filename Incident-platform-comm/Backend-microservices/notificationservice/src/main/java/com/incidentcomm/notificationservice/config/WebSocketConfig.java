package com.incidentcomm.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker for subscriptions
        // /topic for broadcast events, /queue for user-specific events
        config.enableSimpleBroker("/topic", "/queue");

        // Set prefix for controller endpoint mapping
        config.setApplicationDestinationPrefixes("/app");

        // Set prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoints
        registry.addEndpoint("/ws-notification")
                // Enable CORS
                .setAllowedOrigins("*")
                // Fallback options for browsers that don't support WebSockets
                .withSockJS();
    }
}