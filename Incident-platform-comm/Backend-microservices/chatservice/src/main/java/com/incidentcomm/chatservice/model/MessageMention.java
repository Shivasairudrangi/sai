package com.incidentcomm.chatservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_mentions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "mentioned_user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageMention {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "mentioned_user_id")
    private Long mentionedUserId;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Pre-persist hook to set creation time
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Mark mention as read
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    // Constructor with essential fields
    public MessageMention(Long messageId, Long mentionedUserId) {
        this.messageId = messageId;
        this.mentionedUserId = mentionedUserId;
        this.isRead = false;
    }
}