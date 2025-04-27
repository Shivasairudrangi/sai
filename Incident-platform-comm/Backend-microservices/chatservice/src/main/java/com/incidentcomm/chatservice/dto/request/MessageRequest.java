package com.incidentcomm.chatservice.dto.request;

import com.incidentcomm.chatservice.model.MessageType;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class MessageRequest {
    private Long chatRoomId;

    @Size(max = 4000)
    private String content;

    private MessageType type = MessageType.TEXT;

    private Long parentMessageId;

    private List<Long> mentionedUserIds;

    // File attachment metadata (for file messages)
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
}