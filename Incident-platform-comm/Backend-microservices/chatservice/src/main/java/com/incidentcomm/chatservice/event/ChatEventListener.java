package com.incidentcomm.chatservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatEventListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Listen for chat events and broadcast them over WebSocket
    @Async
    @EventListener
    public void handleChatEvent(ChatEvent event) {
        log.info("Processing chat event: {}", event);

        // Broadcast to the appropriate destination based on event type
        switch (event.getType()) {
            case MESSAGE_CREATED:
            case MESSAGE_UPDATED:
            case MESSAGE_DELETED:
            case USER_JOINED:
            case USER_LEFT:
            case USER_TYPING:
            case REACTION_ADDED:
            case REACTION_REMOVED:
                // Send to all users in the chat room
                messagingTemplate.convertAndSend("/topic/room/" + event.getChatRoomId(), event);
                break;

            case MESSAGE_DELIVERED:
            case MESSAGE_READ:
                // Send to all users in the chat room for status updates
                messagingTemplate.convertAndSend("/topic/message/status/" + event.getMessageId(), event);
                break;

            case MENTION:
                // Send both to chat room and specifically to the mentioned user
                messagingTemplate.convertAndSend("/topic/room/" + event.getChatRoomId(), event);
                messagingTemplate.convertAndSendToUser(
                        event.getUserId().toString(),
                        "/queue/mentions",
                        event
                );
                break;

            default:
                log.warn("Unhandled event type: {}", event.getType());
        }
    }
}