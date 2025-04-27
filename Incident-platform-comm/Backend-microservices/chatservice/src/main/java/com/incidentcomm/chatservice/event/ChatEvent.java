package com.incidentcomm.chatservice.event;

import com.incidentcomm.chatservice.dto.response.MessageResponse;
import com.incidentcomm.chatservice.model.Message;
import com.incidentcomm.chatservice.model.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatEvent {
    public enum EventType {
        MESSAGE_CREATED,
        MESSAGE_UPDATED,
        MESSAGE_DELETED,
        MESSAGE_DELIVERED,
        MESSAGE_READ,
        USER_JOINED,
        USER_LEFT,
        USER_TYPING,
        REACTION_ADDED,
        REACTION_REMOVED,
        MENTION
    }

    private EventType type;
    private Long chatRoomId;
    private Long messageId;
    private Long userId;
    private String username;
    private MessageResponse message;
    private Object data;
    private LocalDateTime timestamp;

    public ChatEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatEvent(EventType type, Long chatRoomId, Long messageId, Long userId) {
        this.type = type;
        this.chatRoomId = chatRoomId;
        this.messageId = messageId;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    // Factory methods for different event types

    public static ChatEvent messageCreated(Long chatRoomId, MessageResponse message, Long userId) {
        ChatEvent event = new ChatEvent(EventType.MESSAGE_CREATED, chatRoomId, message.getId(), userId);
        event.setMessage(message);
        return event;
    }

    public static ChatEvent messageUpdated(Long chatRoomId, MessageResponse message, Long userId) {
        ChatEvent event = new ChatEvent(EventType.MESSAGE_UPDATED, chatRoomId, message.getId(), userId);
        event.setMessage(message);
        return event;
    }

    public static ChatEvent messageDeleted(Long chatRoomId, Long messageId, Long userId) {
        return new ChatEvent(EventType.MESSAGE_DELETED, chatRoomId, messageId, userId);
    }

    public static ChatEvent messageDelivered(Long chatRoomId, Long messageId, Long userId) {
        ChatEvent event = new ChatEvent(EventType.MESSAGE_DELIVERED, chatRoomId, messageId, userId);
        return event;
    }

    public static ChatEvent messageRead(Long chatRoomId, Long messageId, Long userId) {
        return new ChatEvent(EventType.MESSAGE_READ, chatRoomId, messageId, userId);
    }

    public static ChatEvent userJoined(Long chatRoomId, Long userId, String username) {
        ChatEvent event = new ChatEvent(EventType.USER_JOINED, chatRoomId, null, userId);
        event.setUsername(username);
        return event;
    }

    public static ChatEvent userLeft(Long chatRoomId, Long userId, String username) {
        ChatEvent event = new ChatEvent(EventType.USER_LEFT, chatRoomId, null, userId);
        event.setUsername(username);
        return event;
    }

    public static ChatEvent userTyping(Long chatRoomId, Long userId, String username, boolean isTyping) {
        ChatEvent event = new ChatEvent(EventType.USER_TYPING, chatRoomId, null, userId);
        event.setUsername(username);
        event.setData(isTyping);
        return event;
    }

    public static ChatEvent reactionAdded(Long chatRoomId, Long messageId, Long userId, String reaction) {
        ChatEvent event = new ChatEvent(EventType.REACTION_ADDED, chatRoomId, messageId, userId);
        event.setData(reaction);
        return event;
    }

    public static ChatEvent reactionRemoved(Long chatRoomId, Long messageId, Long userId, String reaction) {
        ChatEvent event = new ChatEvent(EventType.REACTION_REMOVED, chatRoomId, messageId, userId);
        event.setData(reaction);
        return event;
    }

    public static ChatEvent mention(Long chatRoomId, Long messageId, Long mentionedUserId, Long senderId) {
        ChatEvent event = new ChatEvent(EventType.MENTION, chatRoomId, messageId, mentionedUserId);
        event.setData(senderId);
        return event;
    }
}