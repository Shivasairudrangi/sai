package com.incidentcomm.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "reference_url")
    private String referenceUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_delivered")
    private boolean isDelivered = false;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivered_channel")
    private NotificationChannel deliveredChannel;

    @Column(name = "is_dismissed")
    private boolean isDismissed = false;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    @Column(name = "priority")
    private Integer priority = 3; // 1 = highest, 5 = lowest

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "action_text")
    private String actionText;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "is_silent")
    private boolean isSilent = false;

    // Pre-persist hook to set creation time
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Convenience methods for updating notification state

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markAsDelivered(NotificationChannel channel) {
        this.isDelivered = true;
        this.deliveredAt = LocalDateTime.now();
        this.deliveredChannel = channel;
    }

    public void dismiss() {
        this.isDismissed = true;
        this.dismissedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }
}