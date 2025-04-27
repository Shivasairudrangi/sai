package com.incidentcomm.chatservice.dto.response;

import com.incidentcomm.chatservice.model.Message;
import com.incidentcomm.chatservice.model.MessageMention;
import com.incidentcomm.chatservice.model.MessageReaction;
import com.incidentcomm.chatservice.model.MessageType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MessageResponse {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String senderName; // Will be populated from User Service
    private MessageType type;
    private String content;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Long parentMessageId;
    private MessageResponse parentMessage; // For replies - optional
    private LocalDateTime sentAt;
    private LocalDateTime editedAt;
    private boolean isEdited;
    private boolean isDeleted;
    private Map<String, List<ReactionResponse>> reactions = new HashMap<>();
    private List<MentionResponse> mentions = new ArrayList<>();
    private MessageStatusResponse status = new MessageStatusResponse();

    // Convert Message entity to MessageResponse DTO
    public static MessageResponse fromEntity(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setChatRoomId(message.getChatRoomId());
        response.setSenderId(message.getSenderId());
        response.setType(message.getType());
        response.setContent(message.getContent());
        response.setFileUrl(message.getFileUrl());
        response.setFileName(message.getFileName());
        response.setFileType(message.getFileType());
        response.setFileSize(message.getFileSize());
        response.setParentMessageId(message.getParentMessageId());
        response.setSentAt(message.getSentAt());
        response.setEditedAt(message.getEditedAt());
        response.setEdited(message.isEdited());
        response.setDeleted(message.isDeleted());

        return response;
    }

    // Add reactions to the message response
    public void addReaction(MessageReaction reaction, String username) {
        ReactionResponse reactionResponse = new ReactionResponse();
        reactionResponse.setUserId(reaction.getUserId());
        reactionResponse.setUsername(username);
        reactionResponse.setCreatedAt(reaction.getCreatedAt());

        if (!this.reactions.containsKey(reaction.getReaction())) {
            this.reactions.put(reaction.getReaction(), new ArrayList<>());
        }

        this.reactions.get(reaction.getReaction()).add(reactionResponse);
    }

    // Add mentions to the message response
    public void addMention(MessageMention mention, String username) {
        MentionResponse mentionResponse = new MentionResponse();
        mentionResponse.setUserId(mention.getMentionedUserId());
        mentionResponse.setUsername(username);
        mentionResponse.setRead(mention.isRead());
        mentionResponse.setReadAt(mention.getReadAt());

        this.mentions.add(mentionResponse);
    }

    // Inner class to represent reactions
    @Data
    public static class ReactionResponse {
        private Long userId;
        private String username;
        private LocalDateTime createdAt;
    }

    // Inner class to represent mentions
    @Data
    public static class MentionResponse {
        private Long userId;
        private String username;
        private boolean isRead;
        private LocalDateTime readAt;
    }

    // Inner class to represent message status
    @Data
    public static class MessageStatusResponse {
        private boolean deliveredToAll = false;
        private boolean readByAll = false;
        private int deliveredCount = 0;
        private int readCount = 0;
    }
}