package com.incidentcomm.notificationservice.controller;

import com.incidentcomm.notificationservice.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class WebSocketController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Subscribe to notifications
     */
    @MessageMapping("/notifications/subscribe")
    @SendToUser("/queue/subscriptions")
    public Map<String, Object> subscribeToNotifications(Principal principal) {
        String username = principal.getName();
        log.info("User {} subscribed to notifications", username);

        // In a real implementation, we could track active subscriptions

        return Map.of(
                "status", "subscribed",
                "message", "Successfully subscribed to notifications"
        );
    }

    /**
     * Mark notifications as read
     */
    @MessageMapping("/notifications/mark-read")
    public void markNotificationsAsRead(@Payload Map<String, Object> payload,
                                        Principal principal,
                                        SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = extractUserId(principal);

            if (payload.containsKey("notificationIds")) {
                @SuppressWarnings("unchecked")
                List<Long> notificationIds = (List<Long>) payload.get("notificationIds");
                notificationService.markAsRead(notificationIds, userId);
            } else if (payload.containsKey("markAll") && (Boolean) payload.get("markAll")) {
                notificationService.markAllAsRead(userId);
            }

            log.info("Notifications marked as read by user {}", userId);
        } catch (Exception e) {
            log.error("Error marking notifications as read", e);
        }
    }

    /**
     * Dismiss notifications
     */
    @MessageMapping("/notifications/dismiss")
    public void dismissNotifications(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long userId = extractUserId(principal);

            @SuppressWarnings("unchecked")
            List<Long> notificationIds = (List<Long>) payload.get("notificationIds");

            notificationService.dismissNotifications(notificationIds, userId);

            log.info("Notifications dismissed by user {}", userId);
        } catch (Exception e) {
            log.error("Error dismissing notifications", e);
        }
    }

    /**
     * Heartbeat endpoint for keeping connections alive
     */
    @MessageMapping("/heartbeat")
    @SendToUser("/queue/heartbeat")
    public Map<String, String> heartbeat(Principal principal) {
        return Map.of("status", "alive", "user", principal.getName());
    }

    /**
     * Extract user ID from principal
     */
    private Long extractUserId(Principal principal) {
        // In a real implementation, you would extract the user ID from the principal
        // For now, we'll use a mock value
        return 1L;
    }
}