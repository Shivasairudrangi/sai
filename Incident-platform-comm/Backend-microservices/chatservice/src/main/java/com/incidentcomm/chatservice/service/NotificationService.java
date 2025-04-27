package com.incidentcomm.chatservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.service.user}")
    private String userServiceUrl;

    // Send a direct notification to a user
    public void sendDirectNotification(Long userId, String title, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("type", "direct");

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
        );

        log.info("Sent direct notification to user {}: {}", userId, title);
    }

    // Send a notification about an @mention
    public void sendMentionNotification(Long mentionedUserId, Long senderId, Long chatRoomId) {
        // Get sender name (would call user service in real implementation)
        String senderName = getUsernameById(senderId);

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", "New Mention");
        notification.put("message", senderName + " mentioned you in a chat");
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("type", "mention");
        notification.put("senderId", senderId);
        notification.put("chatRoomId", chatRoomId);

        messagingTemplate.convertAndSendToUser(
                mentionedUserId.toString(),
                "/queue/notifications",
                notification
        );

        log.info("Sent mention notification to user {} from user {}", mentionedUserId, senderId);
    }

    // Send a notification about a new message
    public void sendNewMessageNotification(Long userId, Long senderId, Long chatRoomId, String messagePreview) {
        // Get sender name
        String senderName = getUsernameById(senderId);

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", "New Message");
        notification.put("message", senderName + ": " + messagePreview);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("type", "message");
        notification.put("senderId", senderId);
        notification.put("chatRoomId", chatRoomId);

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
        );

        log.info("Sent new message notification to user {} from user {}", userId, senderId);
    }

    // Notify about user joining a chat room
    public void sendUserJoinedNotification(Long chatRoomId, Long userId) {
        // Get username
        String username = getUsernameById(userId);

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", "User Joined");
        notification.put("message", username + " joined the chat");
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("type", "user_joined");
        notification.put("userId", userId);

        messagingTemplate.convertAndSend(
                "/topic/room/" + chatRoomId + "/notifications",
                notification
        );

        log.info("Sent user joined notification for user {} in room {}", userId, chatRoomId);
    }

    // Notify about user leaving a chat room
    public void sendUserLeftNotification(Long chatRoomId, Long userId) {
        // Get username
        String username = getUsernameById(userId);

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", "User Left");
        notification.put("message", username + " left the chat");
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("type", "user_left");
        notification.put("userId", userId);

        messagingTemplate.convertAndSend(
                "/topic/room/" + chatRoomId + "/notifications",
                notification
        );

        log.info("Sent user left notification for user {} in room {}", userId, chatRoomId);
    }

    // Helper method to get username by user ID
    // In a real application, this would call the User Service
    private String getUsernameById(Long userId) {
        // Mock implementation
        try {
            // In a real implementation, we would call the User Service API
            // String url = userServiceUrl + "/api/users/" + userId;
            // UserDto user = restTemplate.getForObject(url, UserDto.class);
            // return user.getUsername();

            // For now, return a placeholder
            return "User " + userId;
        } catch (Exception e) {
            log.error("Error fetching username for user ID: {}", userId, e);
            return "Unknown User";
        }
    }
}