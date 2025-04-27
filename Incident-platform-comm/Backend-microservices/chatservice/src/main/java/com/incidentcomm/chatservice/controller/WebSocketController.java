package com.incidentcomm.chatservice.controller;

import com.incidentcomm.chatservice.dto.request.MessageRequest;
import com.incidentcomm.chatservice.dto.response.MessageResponse;
import com.incidentcomm.chatservice.dto.response.StatusResponse;
import com.incidentcomm.chatservice.event.ChatEvent;
import com.incidentcomm.chatservice.service.ChatRoomService;
import com.incidentcomm.chatservice.service.MessageService;
import com.incidentcomm.chatservice.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // Send a new message to a chat room
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageRequest messageRequest, Principal principal) {
        try {
            // Extract user ID from principal (assuming it's stored there)
            Long userId = extractUserId(principal);

            if (userId == null) {
                log.error("Unable to extract user ID from principal");
                return;
            }

            // Create the message
            MessageResponse response = messageService.createMessage(messageRequest, userId);

            // The message created event is published in the service
            // and will be handled by the event listener to broadcast to all subscribers

            log.info("Message sent by user {} to room {}", userId, messageRequest.getChatRoomId());
        } catch (Exception e) {
            log.error("Error sending message", e);
            // Send error to the sender
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    new StatusResponse(false, "Error sending message: " + e.getMessage())
            );
        }
    }

    // Typing indicator
    @MessageMapping("/chat.typing")
    @SendTo("/topic/room/{roomId}/typing")
    public ChatEvent sendTypingIndicator(@DestinationVariable Long roomId,
                                         @Payload Map<String, Object> payload,
                                         Principal principal) {
        try {
            Long userId = extractUserId(principal);
            String username = principal.getName(); // In reality, get the actual username
            Boolean isTyping = (Boolean) payload.getOrDefault("isTyping", false);

            return ChatEvent.userTyping(roomId, userId, username, isTyping);
        } catch (Exception e) {
            log.error("Error processing typing indicator", e);
            return null;
        }
    }

    // Mark messages as read
    @MessageMapping("/chat.markRead")
    public void markMessagesAsRead(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long userId = extractUserId(principal);
            Long chatRoomId = Long.valueOf(payload.get("chatRoomId").toString());

            // If specific message IDs are provided
            if (payload.containsKey("messageIds")) {
                @SuppressWarnings("unchecked")
                List<Long> messageIds = (List<Long>) payload.get("messageIds");
                messageService.markMessagesAsRead(messageIds, userId);
            } else {
                // Mark all messages in the room as read
                chatRoomService.markAllMessagesAsRead(chatRoomId, userId);
            }

            log.info("Messages marked as read by user {} in room {}", userId, chatRoomId);
        } catch (Exception e) {
            log.error("Error marking messages as read", e);
        }
    }

    // Join a chat room
    @MessageMapping("/chat.join")
    public void joinChatRoom(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long userId = extractUserId(principal);
            Long chatRoomId = Long.valueOf(payload.get("chatRoomId").toString());
            String username = principal.getName(); // In reality, get the actual username

            // The actual joining logic would be in the ChatRoomService
            // Here we just notify other users

            // Publish user joined event
            eventPublisher.publishEvent(ChatEvent.userJoined(chatRoomId, userId, username));

            log.info("User {} joined chat room {}", userId, chatRoomId);
        } catch (Exception e) {
            log.error("Error joining chat room", e);
        }
    }

    // Leave a chat room
    @MessageMapping("/chat.leave")
    public void leaveChatRoom(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long userId = extractUserId(principal);
            Long chatRoomId = Long.valueOf(payload.get("chatRoomId").toString());
            String username = principal.getName(); // In reality, get the actual username

            // Publish user left event
            eventPublisher.publishEvent(ChatEvent.userLeft(chatRoomId, userId, username));

            log.info("User {} left chat room {}", userId, chatRoomId);
        } catch (Exception e) {
            log.error("Error leaving chat room", e);
        }
    }

    // Add reaction to a message
    @MessageMapping("/chat.addReaction")
    public void addReaction(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long userId = extractUserId(principal);
            Long messageId = Long.valueOf(payload.get("messageId").toString());
            String reaction = payload.get("reaction").toString();

            messageService.addReaction(messageId, reaction, userId);

            log.info("User {} added reaction {} to message {}", userId, reaction, messageId);
        } catch (Exception e) {
            log.error("Error adding reaction", e);
        }
    }

    // Remove reaction from a message
    @MessageMapping("/chat.removeReaction")
    public void removeReaction(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long userId = extractUserId(principal);
            Long messageId = Long.valueOf(payload.get("messageId").toString());
            String reaction = payload.get("reaction").toString();

            messageService.removeReaction(messageId, reaction, userId);

            log.info("User {} removed reaction {} from message {}", userId, reaction, messageId);
        } catch (Exception e) {
            log.error("Error removing reaction", e);
        }
    }

    // Helper method to extract user ID from Principal
    private Long extractUserId(Principal principal) {
        try {
            // In a real application, you would implement a proper way to extract user ID
            // This is a mockup for demonstration
            return Long.valueOf(principal.getName());
        } catch (Exception e) {
            log.error("Error extracting user ID from principal", e);
            return null;
        }
    }
}