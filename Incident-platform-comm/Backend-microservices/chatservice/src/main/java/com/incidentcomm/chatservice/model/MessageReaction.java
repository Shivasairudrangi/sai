package com.incidentcomm.chatservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_reactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "user_id", "reaction"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "reaction", length = 50)
    private String reaction;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Pre-persist hook to set creation time
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructor with essential fields
    public MessageReaction(Long messageId, Long userId, String reaction) {
        this.messageId = messageId;
        this.userId = userId;
        this.reaction = reaction;
    }
}