package com.incidentcomm.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.incidentcomm.apigateway")
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r.path("/api/users/**", "/api/auth/**")
                        .uri("http://localhost:8081"))
                
                // Incident Service Routes
                .route("incident-service", r -> r.path("/api/incidents/**")
                        .uri("http://localhost:8082"))
                
                // Chat Service Routes - includes rooms and messages endpoints
                .route("chat-service", r -> r.path("/api/chats/**", "/api/rooms/**", "/api/messages/**")
                        .uri("http://localhost:8083"))
                
                // File Service Routes - multiple endpoints
                .route("file-service", r -> r.path("/api/files/**", 
                                                  "/api/files/upload/**",
                                                  "/api/files/download/**", 
                                                  "/api/files/tags/**",
                                                  "/api/files/search/**",
                                                  "/api/files/share/**")
                        .uri("http://localhost:8084"))
                
                // Notification Service Routes
                .route("notification-service", r -> r.path("/api/notifications/**",
                                                          "/api/notifications/unread/**",
                                                          "/api/notifications/preferences/**",
                                                          "/api/notifications/mark-read/**",
                                                          "/api/notifications/summary/**")
                        .uri("http://localhost:8085"))
                
                // WebSocket Routes - ensure they are properly forwarded
                .route("chat-ws", r -> r.path("/ws-chat/**")
                        .uri("http://localhost:8083"))
                .route("incident-ws", r -> r.path("/ws-incident/**")
                        .uri("http://localhost:8082"))
                .route("notification-ws", r -> r.path("/ws-notification/**")
                        .uri("http://localhost:8085"))
                .build();
    }
} 