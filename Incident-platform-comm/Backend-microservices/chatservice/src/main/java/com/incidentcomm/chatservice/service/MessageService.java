package com.incidentcomm.chatservice.service;

import com.incidentcomm.chatservice.dto.request.MessageRequest;
import com.incidentcomm.chatservice.dto.response.MessageResponse;
import com.incidentcomm.chatservice.event.ChatEvent;
import com.incidentcomm.chatservice.exception.ResourceNotFoundException;
import com.incidentcomm.chatservice.model.*;
import com.incidentcomm.chatservice.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageStatusRepository messageStatusRepository;

    @Autowired
    private MessageReactionRepository messageReactionRepository;

    @Autowired
    private MessageMentionRepository messageMentionRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRoomUserRepository chatRoomUserRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private NotificationService notificationService;

    // Create a new message
    @Transactional
    public MessageResponse createMessage(MessageRequest request, Long senderId) {
        log.info("Creating message in chat room {}", request.getChatRoomId());

        // Check if chat room exists
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found with id: " + request.getChatRoomId()));

        // Check if sender is a member of the chat room
        Optional<ChatRoomUser> membership = chatRoomUserRepository
                .findByChatRoomIdAndUserId(request.getChatRoomId(), senderId);

        if (membership.isEmpty() || !membership.get().isActive()) {
            throw new IllegalStateException("User is not a member of this chat room");
        }

        // Create message
        Message message = new Message();
        message.setChatRoomId(request.getChatRoomId());
        message.setSenderId(senderId);
        message.setType(request.getType());
        message.setContent(request.getContent());
        message.setParentMessageId(request.getParentMessageId());

        // Set file metadata if present
        if (request.getType() == MessageType.FILE || request.getType() == MessageType.IMAGE) {
            message.setFileUrl(request.getFileUrl());
            message.setFileName(request.getFileName());
            message.setFileType(request.getFileType());
            message.setFileSize(request.getFileSize());
        }

        Message savedMessage = messageRepository.save(message);

        // Create message status for all chat room users except sender
        List<ChatRoomUser> roomUsers = chatRoomUserRepository
                .findByChatRoomIdAndIsActive(request.getChatRoomId(), true);

        for (ChatRoomUser user : roomUsers) {
            if (!user.getUserId().equals(senderId)) {
                MessageStatus status = new MessageStatus();
                status.setMessageId(savedMessage.getId());
                status.setUserId(user.getUserId());
                messageStatusRepository.save(status);
            }
        }

        // Process mentions if present
        Set<Long> mentionedUsers = new HashSet<>();

        // Check explicit mentions in the request
        if (request.getMentionedUserIds() != null && !request.getMentionedUserIds().isEmpty()) {
            mentionedUsers.addAll(request.getMentionedUserIds());
        }

        // Also check for @mentions in the content
        if (request.getContent() != null) {
            // Look for patterns like @username or @123 (user IDs)
            Pattern pattern = Pattern.compile("@(\\w+)");
            Matcher matcher = pattern.matcher(request.getContent());

            while (matcher.find()) {
                String mention = matcher.group(1);
                try {
                    // If it's a user ID
                    Long userId = Long.parseLong(mention);
                    mentionedUsers.add(userId);
                } catch (NumberFormatException e) {
                    // If it's a username, we would need to call user service to resolve it
                    // For simplicity, we'll skip username resolution in this implementation
                }
            }
        }

        // Save all mentions
        for (Long userId : mentionedUsers) {
            // Check if the mentioned user is in the chat room
            Optional<ChatRoomUser> mentionedMembership = chatRoomUserRepository
                    .findByChatRoomIdAndUserId(request.getChatRoomId(), userId);

            if (mentionedMembership.isPresent() && mentionedMembership.get().isActive()) {
                MessageMention mention = new MessageMention(savedMessage.getId(), userId);
                messageMentionRepository.save(mention);

                // Send mention event
                eventPublisher.publishEvent(
                        ChatEvent.mention(request.getChatRoomId(), savedMessage.getId(), userId, senderId));

                // Send notification to the mentioned user
                notificationService.sendMentionNotification(userId, senderId, request.getChatRoomId());
            }
        }

        // Update last activity for the sender
        chatRoomUserRepository.updateLastReadAt(
                request.getChatRoomId(), senderId, LocalDateTime.now());

        // Create message response with all details
        MessageResponse response = getMessageResponseById(savedMessage.getId(), senderId);

        // Publish message created event
        eventPublisher.publishEvent(
                ChatEvent.messageCreated(request.getChatRoomId(), response, senderId));

        return response;
    }

    // Get a message by ID
    public MessageResponse getMessageResponseById(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        MessageResponse response = MessageResponse.fromEntity(message);

        // Set sender name
        response.setSenderName(getUsernameById(message.getSenderId()));

        // Add reactions
        List<MessageReaction> reactions = messageReactionRepository.findByMessageId(messageId);
        for (MessageReaction reaction : reactions) {
            response.addReaction(reaction, getUsernameById(reaction.getUserId()));
        }

        // Add mentions
        List<MessageMention> mentions = messageMentionRepository.findByMessageId(messageId);
        for (MessageMention mention : mentions) {
            response.addMention(mention, getUsernameById(mention.getMentionedUserId()));
        }

        // Get message status
        int totalUsers = (int) chatRoomUserRepository
                .countByChatRoomIdAndIsActive(message.getChatRoomId(), true) - 1; // Exclude sender

        if (totalUsers > 0) {
            List<MessageStatus> statuses = messageStatusRepository.findByMessageId(messageId);

            int deliveredCount = 0;
            int readCount = 0;

            for (MessageStatus status : statuses) {
                if (status.isDelivered()) deliveredCount++;
                if (status.isRead()) readCount++;
            }

            response.getStatus().setDeliveredCount(deliveredCount);
            response.getStatus().setReadCount(readCount);
            response.getStatus().setDeliveredToAll(deliveredCount >= totalUsers);
            response.getStatus().setReadByAll(readCount >= totalUsers);
        } else {
            // No other users to deliver to
            response.getStatus().setDeliveredToAll(true);
            response.getStatus().setReadByAll(true);
        }

        // If the message has a parent message (is a reply), get the parent
        if (message.getParentMessageId() != null) {
            Message parentMessage = messageRepository.findById(message.getParentMessageId())
                    .orElse(null);

            if (parentMessage != null && !parentMessage.isDeleted()) {
                MessageResponse parentResponse = MessageResponse.fromEntity(parentMessage);
                parentResponse.setSenderName(getUsernameById(parentMessage.getSenderId()));
                response.setParentMessage(parentResponse);
            }
        }

        return response;
    }

    // Get messages in a chat room (paginated)
    public Page<MessageResponse> getMessagesInRoom(Long chatRoomId, Long userId, Pageable pageable) {
        // Check if chat room exists
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found with id: " + chatRoomId));

        // Check if user is a member of the chat room
        Optional<ChatRoomUser> membership = chatRoomUserRepository
                .findByChatRoomIdAndUserId(chatRoomId, userId);

        if (membership.isEmpty() || !membership.get().isActive()) {
            throw new IllegalStateException("User is not a member of this chat room");
        }

        // Get messages
        Page<Message> messages = messageRepository
                .findByChatRoomIdAndIsDeletedOrderBySentAtDesc(chatRoomId, false, pageable);

        // Mark messages as delivered
        markMessagesAsDelivered(chatRoomId, userId);

        // Convert to response DTOs
        return messages.map(message -> getMessageResponseById(message.getId(), userId));
    }

    // Update a message
    @Transactional
    public MessageResponse updateMessage(Long messageId, MessageRequest request, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        // Check if user is the sender
        if (!message.getSenderId().equals(userId)) {
            throw new IllegalStateException("Only the sender can edit the message");
        }

        // Check if message is not a system message
        if (message.getType() == MessageType.SYSTEM) {
            throw new IllegalStateException("System messages cannot be edited");
        }

        // Update fields
        if (request.getContent() != null) {
            message.setContent(request.getContent());
        }

        message.markAsEdited();
        Message updatedMessage = messageRepository.save(message);

        // Get updated response
        MessageResponse response = getMessageResponseById(updatedMessage.getId(), userId);

        // Publish message updated event
        eventPublisher.publishEvent(
                ChatEvent.messageUpdated(message.getChatRoomId(), response, userId));

        return response;
    }

    // Delete a message (soft delete)
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        // Check if user is the sender or an admin
        if (!message.getSenderId().equals(userId)) {
            boolean isAdmin = chatRoomUserRepository
                    .existsByChatRoomIdAndUserIdAndIsAdmin(message.getChatRoomId(), userId, true);

            if (!isAdmin) {
                throw new IllegalStateException("Only the sender or an admin can delete the message");
            }
        }

        // Soft delete
        message.setDeleted(true);
        messageRepository.save(message);

        // Publish message deleted event
        eventPublisher.publishEvent(
                ChatEvent.messageDeleted(message.getChatRoomId(), messageId, userId));
    }

    // Add a reaction to a message
    @Transactional
    public MessageResponse addReaction(Long messageId, String reaction, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        // Check if user is a member of the chat room
        Optional<ChatRoomUser> membership = chatRoomUserRepository
                .findByChatRoomIdAndUserId(message.getChatRoomId(), userId);

        if (membership.isEmpty() || !membership.get().isActive()) {
            throw new IllegalStateException("User is not a member of this chat room");
        }

        // Check if the reaction already exists
        Optional<MessageReaction> existingReaction = messageReactionRepository
                .findByMessageIdAndUserIdAndReaction(messageId, userId, reaction);

        if (existingReaction.isPresent()) {
            // Reaction already exists, do nothing
            return getMessageResponseById(messageId, userId);
        }

        // Add the reaction
        MessageReaction newReaction = new MessageReaction(messageId, userId, reaction);
        messageReactionRepository.save(newReaction);

        // Publish reaction added event
        eventPublisher.publishEvent(
                ChatEvent.reactionAdded(message.getChatRoomId(), messageId, userId, reaction));

        return getMessageResponseById(messageId, userId);
    }

    // Remove a reaction from a message
    @Transactional
    public MessageResponse removeReaction(Long messageId, String reaction, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        // Delete the reaction
        messageReactionRepository.deleteByMessageIdAndUserIdAndReaction(messageId, userId, reaction);

        // Publish reaction removed event
        eventPublisher.publishEvent(
                ChatEvent.reactionRemoved(message.getChatRoomId(), messageId, userId, reaction));

        return getMessageResponseById(messageId, userId);
    }

    // Mark messages as delivered for a user
    @Transactional
    public void markMessagesAsDelivered(Long chatRoomId, Long userId) {
        // Find all undelivered messages in the chat room
        List<Message> messages = messageRepository
                .findByChatRoomIdAndIsDeletedOrderBySentAtDesc(chatRoomId, false, Pageable.unpaged())
                .getContent();

        if (messages.isEmpty()) {
            return;
        }

        List<Long> messageIds = messages.stream()
                .map(Message::getId)
                .collect(Collectors.toList());

        int updatedCount = messageStatusRepository.markMessagesAsDelivered(
                userId, messageIds, LocalDateTime.now());

        if (updatedCount > 0) {
            log.info("Marked {} messages as delivered for user {} in room {}",
                    updatedCount, userId, chatRoomId);

            // Publish delivery events
            for (Message message : messages) {
                eventPublisher.publishEvent(
                        ChatEvent.messageDelivered(chatRoomId, message.getId(), userId));
            }
        }
    }

    // Mark messages as read for a user
    @Transactional
    public void markMessagesAsRead(List<Long> messageIds, Long userId) {
        if (messageIds.isEmpty()) {
            return;
        }

        int updatedCount = messageStatusRepository.markMessagesAsRead(
                userId, messageIds, LocalDateTime.now());

        if (updatedCount > 0) {
            log.info("Marked {} messages as read for user {}", updatedCount, userId);

            // Get the chat room ID (assuming all messages are from the same room)
            Optional<Message> firstMessage = messageRepository.findById(messageIds.get(0));
            if (firstMessage.isPresent()) {
                Long chatRoomId = firstMessage.get().getChatRoomId();

                // Update last read timestamp in chat room
                chatRoomUserRepository.updateLastReadAt(chatRoomId, userId, LocalDateTime.now());

                // Publish read events
                for (Long messageId : messageIds) {
                    eventPublisher.publishEvent(
                            ChatEvent.messageRead(chatRoomId, messageId, userId));
                }
            }
        }
    }

    // Mark all messages in a chat room as read
    @Transactional
    public void markAllMessagesAsReadInRoom(Long chatRoomId, Long userId) {
        // Find all unread messages in the chat room
        List<Message> messages = messageRepository
                .findByChatRoomIdAndIsDeletedOrderBySentAtDesc(chatRoomId, false, Pageable.unpaged())
                .getContent();

        if (messages.isEmpty()) {
            return;
        }

        List<Long> messageIds = messages.stream()
                .map(Message::getId)
                .collect(Collectors.toList());

        markMessagesAsRead(messageIds, userId);
    }

    // Search messages in a chat room
    public Page<MessageResponse> searchMessages(Long chatRoomId, String keyword, Long userId, Pageable pageable) {
        // Check if chat room exists
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found with id: " + chatRoomId));

        // Check if user is a member of the chat room
        Optional<ChatRoomUser> membership = chatRoomUserRepository
                .findByChatRoomIdAndUserId(chatRoomId, userId);

        if (membership.isEmpty() || !membership.get().isActive()) {
            throw new IllegalStateException("User is not a member of this chat room");
        }

        // Search messages
        Page<Message> messages = messageRepository.searchMessages(chatRoomId, keyword, pageable);

        // Convert to response DTOs
        return messages.map(message -> getMessageResponseById(message.getId(), userId));
    }

    // Get file attachments in a chat room
    public Page<MessageResponse> getFileAttachments(Long chatRoomId, String fileType, Long userId, Pageable pageable) {
        // Check if chat room exists
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found with id: " + chatRoomId));

        // Check if user is a member of the chat room
        Optional<ChatRoomUser> membership = chatRoomUserRepository
                .findByChatRoomIdAndUserId(chatRoomId, userId);

        if (membership.isEmpty() || !membership.get().isActive()) {
            throw new IllegalStateException("User is not a member of this chat room");
        }

        // Get file attachments
        Page<Message> messages;
        if (fileType != null && !fileType.trim().isEmpty()) {
            messages = messageRepository.findFileAttachmentsByType(chatRoomId, fileType, pageable);
        } else {
            messages = messageRepository.findFileAttachments(chatRoomId, pageable);
        }

        // Convert to response DTOs
        return messages.map(message -> getMessageResponseById(message.getId(), userId));
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