package com.incidentcomm.chatservice.controller;

import com.incidentcomm.chatservice.dto.request.ChatRoomRequest;
import com.incidentcomm.chatservice.dto.request.MessageRequest;
import com.incidentcomm.chatservice.dto.response.ChatRoomResponse;
import com.incidentcomm.chatservice.dto.response.MessageResponse;
import com.incidentcomm.chatservice.dto.response.StatusResponse;
import com.incidentcomm.chatservice.service.ChatRoomService;
import com.incidentcomm.chatservice.service.MessageService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class ChatController {

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private MessageService messageService;

    // === Chat Room Endpoints ===

    // Create a new chat room
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createChatRoom(
            @Valid @RequestBody ChatRoomRequest request,
            @RequestHeader("X-User-ID") Long userId) {

        ChatRoomResponse chatRoom = chatRoomService.createChatRoom(request, userId);
        return ResponseEntity.ok(chatRoom);
    }

    // Get a chat room by ID
    @GetMapping("/rooms/{id}")
    public ResponseEntity<ChatRoomResponse> getChatRoomById(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") Long userId) {

        ChatRoomResponse chatRoom = chatRoomService.getChatRoomById(id, userId);
        return ResponseEntity.ok(chatRoom);
    }

    // Get all chat rooms for a user
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getChatRoomsForUser(
            @RequestHeader("X-User-ID") Long userId) {

        List<ChatRoomResponse> chatRooms = chatRoomService.getChatRoomsForUser(userId);
        return ResponseEntity.ok(chatRooms);
    }

    // Update a chat room
    @PutMapping("/rooms/{id}")
    public ResponseEntity<ChatRoomResponse> updateChatRoom(
            @PathVariable Long id,
            @Valid @RequestBody ChatRoomRequest request,
            @RequestHeader("X-User-ID") Long userId) {

        ChatRoomResponse chatRoom = chatRoomService.updateChatRoom(id, request, userId);
        return ResponseEntity.ok(chatRoom);
    }

    // Add users to a chat room
    @PostMapping("/rooms/{id}/users")
    public ResponseEntity<ChatRoomResponse> addUsersToRoom(
            @PathVariable Long id,
            @RequestBody List<Long> userIds,
            @RequestHeader("X-User-ID") Long userId) {

        ChatRoomResponse chatRoom = chatRoomService.addUsersToRoom(id, userIds, userId);
        return ResponseEntity.ok(chatRoom);
    }

    // Remove a user from a chat room
    @DeleteMapping("/rooms/{roomId}/users/{userId}")
    public ResponseEntity<StatusResponse> removeUserFromRoom(
            @PathVariable Long roomId,
            @PathVariable Long userId,
            @RequestHeader("X-User-ID") Long requesterId) {

        chatRoomService.removeUserFromRoom(roomId, userId, requesterId);
        return ResponseEntity.ok(new StatusResponse(true, "User removed from chat room"));
    }

    // Get chat room for an incident
    @GetMapping("/incidents/{incidentId}/chat")
    public ResponseEntity<ChatRoomResponse> getChatRoomForIncident(
            @PathVariable Long incidentId,
            @RequestHeader("X-User-ID") Long userId) {

        ChatRoomResponse chatRoom = chatRoomService.getChatRoomForIncident(incidentId, userId);
        return ResponseEntity.ok(chatRoom);
    }

    // Mark all messages in a chat room as read
    @PostMapping("/rooms/{id}/read")
    public ResponseEntity<StatusResponse> markAllMessagesAsRead(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") Long userId) {

        chatRoomService.markAllMessagesAsRead(id, userId);
        return ResponseEntity.ok(new StatusResponse(true, "All messages marked as read"));
    }

    // === Message Endpoints ===

    // Create a new message
    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> createMessage(
            @Valid @RequestBody MessageRequest request,
            @RequestHeader("X-User-ID") Long userId) {

        MessageResponse message = messageService.createMessage(request, userId);
        return ResponseEntity.ok(message);
    }

    // Get a message by ID
    @GetMapping("/messages/{id}")
    public ResponseEntity<MessageResponse> getMessageById(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") Long userId) {

        MessageResponse message = messageService.getMessageResponseById(id, userId);
        return ResponseEntity.ok(message);
    }

    // Get messages in a chat room (paginated)
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<MessageResponse>> getMessagesInRoom(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sentAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader("X-User-ID") Long userId) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<MessageResponse> messages = messageService.getMessagesInRoom(roomId, userId, pageable);

        return ResponseEntity.ok(messages);
    }

    // Update a message
    @PutMapping("/messages/{id}")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable Long id,
            @Valid @RequestBody MessageRequest request,
            @RequestHeader("X-User-ID") Long userId) {

        MessageResponse message = messageService.updateMessage(id, request, userId);
        return ResponseEntity.ok(message);
    }

    // Delete a message
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<StatusResponse> deleteMessage(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") Long userId) {

        messageService.deleteMessage(id, userId);
        return ResponseEntity.ok(new StatusResponse(true, "Message deleted"));
    }

    // Add a reaction to a message
    @PostMapping("/messages/{id}/reactions/{reaction}")
    public ResponseEntity<MessageResponse> addReaction(
            @PathVariable Long id,
            @PathVariable String reaction,
            @RequestHeader("X-User-ID") Long userId) {

        MessageResponse message = messageService.addReaction(id, reaction, userId);
        return ResponseEntity.ok(message);
    }

    // Remove a reaction from a message
    @DeleteMapping("/messages/{id}/reactions/{reaction}")
    public ResponseEntity<MessageResponse> removeReaction(
            @PathVariable Long id,
            @PathVariable String reaction,
            @RequestHeader("X-User-ID") Long userId) {

        MessageResponse message = messageService.removeReaction(id, reaction, userId);
        return ResponseEntity.ok(message);
    }

    // Search messages in a chat room
    @GetMapping("/rooms/{roomId}/messages/search")
    public ResponseEntity<Page<MessageResponse>> searchMessages(
            @PathVariable Long roomId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-User-ID") Long userId) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MessageResponse> messages = messageService.searchMessages(roomId, keyword, userId, pageable);

        return ResponseEntity.ok(messages);
    }

    // Get file attachments in a chat room
    @GetMapping("/rooms/{roomId}/files")
    public ResponseEntity<Page<MessageResponse>> getFileAttachments(
            @PathVariable Long roomId,
            @RequestParam(required = false) String fileType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-User-ID") Long userId) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MessageResponse> files = messageService.getFileAttachments(roomId, fileType, userId, pageable);

        return ResponseEntity.ok(files);
    }
}