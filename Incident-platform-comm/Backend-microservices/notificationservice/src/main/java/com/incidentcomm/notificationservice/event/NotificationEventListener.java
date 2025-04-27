package com.incidentcomm.notificationservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationEventListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle notification events and broadcast them via WebSocket
     */
    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.debug("Processing notification event: {} for user {}", event.getType(), event.getUserId());

        // Broadcast the event to the specific user's queue
        messagingTemplate.convertAndSendToUser(
                event.getUserId().toString(),
                "/queue/notification-events",
                event
        );

        // For certain events, broadcast to a global topic for monitoring
        switch (event.getType()) {
            case CREATED:
            case ALL_READ:
                messagingTemplate.convertAndSend("/topic/notification-summary", event);
                break;
        }
    }
}