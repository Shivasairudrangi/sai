package com.incidentcomm.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "notification_type", "channel"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "quiet_hours_start")
    private Integer quietHoursStart;

    @Column(name = "quiet_hours_end")
    private Integer quietHoursEnd;

    // For email/SMS channels
    @Column(name = "delivery_address")
    private String deliveryAddress;

    // Pre-persist/pre-update hook to set updated time
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Check if notification should be sent based on current time and quiet hours
    public boolean shouldSendNow() {
        // If quiet hours are not set, always send
        if (quietHoursStart == null || quietHoursEnd == null) {
            return true;
        }

        // Check if current time is within quiet hours
        int currentHour = LocalDateTime.now().getHour();

        if (quietHoursStart < quietHoursEnd) {
            // Simple case: quiet hours within the same day
            return currentHour < quietHoursStart || currentHour >= quietHoursEnd;
        } else {
            // Quiet hours span midnight
            return currentHour < quietHoursStart && currentHour >= quietHoursEnd;
        }
    }

    // Convenience constructor for essential fields
    public NotificationPreference(Long userId, NotificationType notificationType,
                                  NotificationChannel channel, boolean isEnabled) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.channel = channel;
        this.isEnabled = isEnabled;
        this.updatedAt = LocalDateTime.now();
    }
}