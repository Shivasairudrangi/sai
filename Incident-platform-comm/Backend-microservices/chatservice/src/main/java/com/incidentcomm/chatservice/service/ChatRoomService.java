package com.incidentcomm.chatservice.service;

import com.incidentcomm.chatservice.dto.request.ChatRoomRequest;
import com.incidentcomm.chatservice.dto.response.ChatRoomResponse;
import com.incidentcomm.chatservice.dto.response.MessageResponse;
import com.incidentcomm.chatservice.event.ChatEvent;
import com.incidentcomm.chatservice.exception.ResourceNotFoundException;
import com.incidentcomm.chatservice.model.ChatRoom;
import com.incidentcomm.chatservice.model.ChatRoomUser;
import com.incidentcomm.chatservice.model.Message;
import com.incidentcomm.chatservice.repository.ChatRoomRepository;
import com.incidentcomm.chatservice.repository.ChatRoomUserRepository;
import com.incidentcomm.chatservice.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRoomUserRepository chatRoomUserRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MessageService messageService;

    // Create a new chat room
    @Transactional
    public ChatRoomResponse createChatRoom(ChatRoomRequest request, Long creatorId) {
        log.info("Creating chat room: {}", request.getName());

        // Check if a direct chat already exists between two users (for non-group chats)
        if (!request.isGroup() && request.getUserIds().size() == 1) {
            Long otherUserId = request.getUserIds().get(0);
            Optional<ChatRoom> existingRoom = chatRoomRepository
                    .findDirectChatBetweenUsers(creatorId, otherUserId);

            if (existingRoom.isPresent()) {
                log.info("Direct chat already exists between users {} and {}", creatorId, otherUserId);
                return getChatRoomById(existingRoom.get().getId(), creatorId);
            }
        }

        // Create new chat room
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(request.getName());
        chatRoom.setDescription(request.getDescription());
        chatRoom.setCreatedBy(creatorId);
        chatRoom.setGroup(request.isGroup());
        chatRoom.setIncidentRelated(request.isIncidentRelated());
        chatRoom.setIncidentId(request.getIncidentId());

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // Add creator as an admin
        ChatRoomUser creatorMembership = new ChatRoomUser();
        creatorMembership.setChatRoomId(savedChatRoom.getId());
        creatorMembership.setUserId(creatorId);
        creatorMembership.setAdmin(true);
        chatRoomUserRepository.save(creatorMembership);

        // Add other users to the chat room
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            for (Long userId : request.getUserIds()) {
                if (!userId.equals(creatorId)) {
                    ChatRoomUser membership = new ChatRoomUser();
                    membership.setChatRoomId(savedChatRoom.getId());
                    membership.setUserId(userId);
                    membership.setAdmin(false);
                    chatRoomUserRepository.save(membership);

                    // Publish user joined event
                    // Here we would get the username from user service
                    String username = getUsernameById(userId);
                    eventPublisher.publishEvent(
                            ChatEvent.userJoined(savedChatRoom.getId(), userId, username));
                }
            }
        }

        ChatRoomResponse response = ChatRoomResponse.fromEntity(savedChatRoom);

        // Add users to response
        List<ChatRoomUser> users = chatRoomUserRepository.findByChatRoomIdAndIsActive(
                savedChatRoom.getId(), true);

        for (ChatRoomUser user : users) {
            String username = getUsernameById(user.getUserId());
            response.addUser(user, username);
        }

        return response;
    }

    // Get chat room by ID
    public ChatRoomResponse getChatRoomById(Long chatRoomId, Long currentUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found with id: " + chatRoomId));

        // Check if user is a member of the chat room
        Optional<ChatRoomUser> membership = chatRoomUserRepository.findByChatRoomIdAndUserId(chatRoomId, currentUserId);
        if (membership.isEmpty() || !membership.get().isActive()) {
            throw new ResourceNotFoundException("User is not a member of this chat room");
        }

        ChatRoomResponse response = ChatRoomResponse.fromEntity(chatRoom);

        // Add users to response
        List<ChatRoomUser> users = chatRoomUserRepository.findByChatRoomIdAndIsActive(chatRoomId, true);
        for (ChatRoomUser user : users) {
            String username = getUsernameById(user.getUserId());
            response.addUser(user, username);
        }

        // Add last message
        List<Message> recentMessages = messageRepository
                .findTop20ByChatRoomIdAndIsDeletedOrderBySentAtDesc(chatRoomId, false);

        if (!recentMessages.isEmpty()) {
            Message lastMessage = recentMessages.get(0);
            MessageResponse messageResponse = messageService.getMessageResponseById(lastMessage.getId(), currentUserId);
            response.setLastMessage(messageResponse);
        }

        // Set unread message count
        long unreadCount = messageRepository.countUnreadMessages(chatRoomId, currentUserId);
        response.setUnreadMessageCount(unreadCount);

        return response;
    }

    // Get all chat rooms for a user
    public List<ChatRoomResponse> getChatRoomsForUser(Long userId) {
        List<ChatRoomUser> memberships = chatRoomUserRepository.findByUserIdAndIsActive(userId, true);

        List<ChatRoomResponse> response = new ArrayList<>();

        for (ChatRoomUser membership : memberships) {
            ChatRoom chatRoom = chatRoomRepository.findById(membership.getChatRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Chat room not found with id: " + membership.getChatRoomId()));

            if (chatRoom.isActive()) {
                ChatRoomResponse roomResponse = ChatRoomResponse.fromEntity(chatRoom);

                // Add unread message count
                long unreadCount = messageRepository.countUnreadMessages(chatRoom.getId(), userId);
                roomResponse.setUnreadMessageCount(unreadCount);

                // Add last message if exists
                List<Message> recentMessages = messageRepository
                        .findTop20ByChatRoomIdAndIsDeletedOrderBySentAtDesc(chatRoom.getId(), false);

                if (!recentMessages.isEmpty()) {
                    Message lastMessage = recentMessages.get(0);
                    MessageResponse messageResponse = messageService.getMessageResponseById(lastMessage.getId(), userId);
                    roomResponse.setLastMessage(messageResponse);
                }

                // Add users to response (only basic info to keep the response light)
                roomResponse.setUsers(new ArrayList<>());

                response.add(roomResponse);
            }
        }

        return response;
    }

    // Update chat room details
    @Transactional
    public ChatRoomResponse updateChatRoom(Long chatRoomId, ChatRoomRequest request, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found with id: " + chatRoomId));

        // Check if user is an admin
        boolean isAdmin = chatRoomUserRepository.existsByChatRoomIdAndUserIdAndIsAdmin(
                chatRoomId, userId, true);

        if (!isAdmin) {
            throw new IllegalStateException("Only admins can update chat room details");
        }

        // Update chat room details
        if (request.getName() != null && !request.getName().isEmpty()) {
            chatRoom.setName(request.getName());
        }

        if (request.getDescription() != null) {
            chatRoom.setDescription(request.getDescription());
        }

        ChatRoom updatedChatRoom = chatRoomRepository.save(chatRoom);

        return getChatRoomById(updatedChatRoom.getId(), userId);
    }

    // Add users to a chat room
    @Transactional
    public ChatRoomResponse addUsersToRoom(Long chatRoomId, List<Long> userIds, Long adminId) {
        // Check if chat room exists
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found with id: " + chatRoomId));

        // Check if admin is an admin
        boolean isAdmin = chatRoomUserRepository.existsByChatRoomIdAndUserIdAndIsAdmin(
                chatRoomId, adminId, true);

        if (!isAdmin) {
            throw new IllegalStateException("Only admins can add users to a chat room");
        }

        for (Long userId : userIds) {
            // Check if user is already in the room
            Optional<ChatRoomUser> existingMember = chatRoomUserRepository
                    .findByChatRoomIdAndUserId(chatRoomId, userId);

            if (existingMember.isPresent()) {
                if (!existingMember.get().isActive()) {
                    // Reactivate the user if they were previously removed
                    existingMember.get().setActive(true);
                    chatRoomUserRepository.save(existingMember.get());

                    // Publish user joined event
                    String username = getUsernameById(userId);
                    eventPublisher.publishEvent(
                            ChatEvent.userJoined(chatRoomId, userId, username));
                }
            } else {
                // Add the new user
                ChatRoomUser newMember = new ChatRoomUser();
                newMember.setChatRoomId(chatRoomId);
                newMember.setUserId(userId);
                newMember.setAdmin(false);
                chatRoomUserRepository.save(newMember);

                // Publish user joined event
                String username = getUsernameById(userId);
                eventPublisher.publishEvent(
                        ChatEvent.userJoined(chatRoomId, userId, username));
            }
        }

        return getChatRoomById(chatRoomId, adminId);
    }

    // Remove a user from a chat room
    @Transactional
    public void removeUserFromRoom(Long chatRoomId, Long userIdToRemove, Long requesterId) {
        // Check if chat room exists
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found with id: " + chatRoomId));

        // User can remove themselves, or an admin can remove someone
        boolean isAdmin = chatRoomUserRepository.existsByChatRoomIdAndUserIdAndIsAdmin(
                chatRoomId, requesterId, true);

        if (!requesterId.equals(userIdToRemove) && !isAdmin) {
            throw new IllegalStateException("Only admins can remove other users from a chat room");
        }

        // Find the user membership
        ChatRoomUser membership = chatRoomUserRepository.findByChatRoomIdAndUserId(chatRoomId, userIdToRemove)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this chat room"));

        // Mark as inactive
        membership.setActive(false);
        chatRoomUserRepository.save(membership);

        // Get username for event
        String username = getUsernameById(userIdToRemove);

        // Publish user left event
        eventPublisher.publishEvent(
                ChatEvent.userLeft(chatRoomId, userIdToRemove, username));
    }

    // Get chat room for an incident
    public ChatRoomResponse getChatRoomForIncident(Long incidentId, Long userId) {
        Optional<ChatRoom> existingRoom = chatRoomRepository
                .findByIncidentIdAndIsIncidentRelated(incidentId, true);

        if (existingRoom.isPresent()) {
            // Check if user is a member of the chat room
            Optional<ChatRoomUser> membership = chatRoomUserRepository.findByChatRoomIdAndUserId(
                    existingRoom.get().getId(), userId);

            // If not a member, add them
            if (membership.isEmpty()) {
                ChatRoomUser newMember = new ChatRoomUser();
                newMember.setChatRoomId(existingRoom.get().getId());
                newMember.setUserId(userId);
                newMember.setAdmin(false);
                chatRoomUserRepository.save(newMember);

                // Publish user joined event
                String username = getUsernameById(userId);
                eventPublisher.publishEvent(
                        ChatEvent.userJoined(existingRoom.get().getId(), userId, username));
            } else if (!membership.get().isActive()) {
                // Reactivate if inactive
                membership.get().setActive(true);
                chatRoomUserRepository.save(membership.get());

                // Publish user joined event
                String username = getUsernameById(userId);
                eventPublisher.publishEvent(
                        ChatEvent.userJoined(existingRoom.get().getId(), userId, username));
            }

            return getChatRoomById(existingRoom.get().getId(), userId);
        } else {
            // Create a new incident chat room
            ChatRoomRequest request = new ChatRoomRequest();
            request.setName("Incident #" + incidentId);
            request.setDescription("Chat room for incident #" + incidentId);
            request.setGroup(true);
            request.setIncidentRelated(true);
            request.setIncidentId(incidentId);

            return createChatRoom(request, userId);
        }
    }

    // Mark all messages in a chat room as read
    @Transactional
    public void markAllMessagesAsRead(Long chatRoomId, Long userId) {
        // Update last read timestamp
        chatRoomUserRepository.updateLastReadAt(chatRoomId, userId, LocalDateTime.now());

        // The actual message status update is handled in the MessageService
        messageService.markAllMessagesAsReadInRoom(chatRoomId, userId);
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