package com.incidentcomm.chatservice.dto.response;

import com.incidentcomm.chatservice.model.ChatRoom;
import com.incidentcomm.chatservice.model.ChatRoomUser;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChatRoomResponse {
    private Long id;
    private String name;
    private String description;
    private Long createdBy;
    private String creatorName; // Will be populated from User Service
    private boolean isGroup;
    private boolean isIncidentRelated;
    private Long incidentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private List<ChatRoomUserResponse> users = new ArrayList<>();
    private Long unreadMessageCount;
    private MessageResponse lastMessage;

    // Convert ChatRoom entity to ChatRoomResponse DTO
    public static ChatRoomResponse fromEntity(ChatRoom chatRoom) {
        ChatRoomResponse response = new ChatRoomResponse();
        response.setId(chatRoom.getId());
        response.setName(chatRoom.getName());
        response.setDescription(chatRoom.getDescription());
        response.setCreatedBy(chatRoom.getCreatedBy());
        response.setGroup(chatRoom.isGroup());
        response.setIncidentRelated(chatRoom.isIncidentRelated());
        response.setIncidentId(chatRoom.getIncidentId());
        response.setCreatedAt(chatRoom.getCreatedAt());
        response.setUpdatedAt(chatRoom.getUpdatedAt());
        response.setActive(chatRoom.isActive());

        return response;
    }

    // Add a user to the chat room response
    public void addUser(ChatRoomUser chatRoomUser, String username) {
        ChatRoomUserResponse userResponse = new ChatRoomUserResponse();
        userResponse.setId(chatRoomUser.getUserId());
        userResponse.setUsername(username);
        userResponse.setNickname(chatRoomUser.getNickname());
        userResponse.setAdmin(chatRoomUser.isAdmin());
        userResponse.setJoinedAt(chatRoomUser.getJoinedAt());
        userResponse.setLastReadAt(chatRoomUser.getLastReadAt());

        this.users.add(userResponse);
    }

    // Inner class to represent users in a chat room
    @Data
    public static class ChatRoomUserResponse {
        private Long id;
        private String username;
        private String nickname;
        private boolean isAdmin;
        private LocalDateTime joinedAt;
        private LocalDateTime lastReadAt;
    }
}