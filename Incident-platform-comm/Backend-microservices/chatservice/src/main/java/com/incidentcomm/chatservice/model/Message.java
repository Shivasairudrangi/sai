package com.incidentcomm.chatservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(name = "sender_id")
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private MessageType type;

    @Size(max = 4000)
    @Column(length = 4000)
    private String content;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "parent_message_id")
    private Long parentMessageId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "is_edited")
    private boolean isEdited;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    // Pre-persist hook to set creation time
    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }

    // Edited flag and timestamp setter
    public void markAsEdited() {
        this.isEdited = true;
        this.editedAt = LocalDateTime.now();
    }
}