package com.incidentcomm.incidentservice.service;

import com.incidentcomm.incidentservice.dto.response.MessageResponse;
import com.incidentcomm.incidentservice.event.IncidentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class IncidentNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${app.service.user}")
    private String userServiceUrl;

    // Send a notification to a specific user
    public void notifyUser(Long userId, String message) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                new MessageResponse(message)
        );
        log.info("Notification sent to user {}: {}", userId, message);
    }

    // Send a notification to users with a specific role
    public void notifyRole(String role, String message) {
        messagingTemplate.convertAndSend(
                "/topic/role/" + role,
                new MessageResponse(message)
        );
        log.info("Notification sent to role {}: {}", role, message);
    }

    // Send a notification to all users about an incident event
    public void broadcastIncidentEvent(IncidentEvent event) {
        // Send to specific incident topic
        messagingTemplate.convertAndSend("/topic/incident/" + event.getIncidentId(), event);

        // Send to incidents topic for dashboard updates
        messagingTemplate.convertAndSend("/topic/incidents", event);

        log.info("Incident event broadcast: {}", event);
    }

    // Send a custom notification to an incident-specific topic
    public void sendIncidentUpdate(Long incidentId, String message, Map<String, Object> data) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("data", data);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/incident/" + incidentId, payload);
        log.info("Custom update sent for incident {}: {}", incidentId, message);
    }

    // Notify when a high priority incident is created
    public void notifyHighPriorityIncident(Long incidentId, String title) {
        String message = "High priority incident #" + incidentId + " created: " + title;

        // Notify managers
        notifyRole("MANAGER", message);

        // Notify administrators
        notifyRole("ADMIN", message);

        log.info("High priority notification sent: {}", message);
    }
}