package com.incidentcomm.incidentservice.controller;

import com.incidentcomm.incidentservice.dto.response.MessageResponse;
import com.incidentcomm.incidentservice.event.IncidentEvent;
import com.incidentcomm.incidentservice.service.IncidentNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@Slf4j
public class WebSocketController {

    @Autowired
    private IncidentNotificationService notificationService;

    // Subscribe to real-time updates for a specific incident
    @MessageMapping("/incident/{incidentId}/subscribe")
    @SendToUser("/queue/subscriptions")
    public MessageResponse subscribeToIncident(@DestinationVariable Long incidentId,
                                               Principal principal) {
        // Principal contains user info
        String username = principal.getName();
        log.info("User {} subscribed to updates for incident #{}", username, incidentId);

        // Here we would update a user subscription repository if needed

        return new MessageResponse("Successfully subscribed to incident #" + incidentId);
    }

    // Send a message to all users tracking a specific incident
    @MessageMapping("/incident/{incidentId}/message")
    @SendTo("/topic/incident/{incidentId}")
    public Map<String, Object> sendIncidentMessage(@DestinationVariable Long incidentId,
                                                   @Payload Map<String, Object> message,
                                                   SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        String username = principal != null ? principal.getName() : "Unknown";

        log.info("Message from {} to incident #{}: {}", username, incidentId, message);

        // Add sender info
        message.put("sender", username);
        message.put("timestamp", System.currentTimeMillis());

        return message;
    }

    // Broadcast incident status update
    @MessageMapping("/incident/{incidentId}/status")
    @SendTo("/topic/incident/{incidentId}")
    public IncidentEvent updateIncidentStatus(@DestinationVariable Long incidentId,
                                              @Payload IncidentEvent event,
                                              SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        String username = principal != null ? principal.getName() : "Unknown";

        log.info("Status update from {} for incident #{}: {}", username, incidentId, event);

        // Return the event to be broadcast to all subscribers
        return event;
    }

    // Heartbeat endpoint for keeping connections alive
    @MessageMapping("/heartbeat")
    @SendToUser("/queue/heartbeat")
    public MessageResponse heartbeat(Principal principal) {
        String username = principal.getName();
        log.debug("Heartbeat received from {}", username);
        return new MessageResponse("pong");
    }
}