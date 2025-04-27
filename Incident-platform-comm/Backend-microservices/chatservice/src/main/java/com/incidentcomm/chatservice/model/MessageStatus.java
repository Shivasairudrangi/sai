package com.incidentcomm.chatservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_statuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "is_delivered")
    private boolean isDelivered;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // Method to mark a message as delivered
    public void markAsDelivered() {
        this.isDelivered = true;
        this.deliveredAt = LocalDateTime.now();
    }

    // Method to mark a message as read
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();

        // Also ensure it's marked as delivered
        if (!this.isDelivered) {
            markAsDelivered();
        }
    }
}