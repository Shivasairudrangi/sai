package com.incidentcomm.notificationservice.event;

import com.incidentcomm.notificationservice.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    public enum EventType {
        CREATED,
        DELIVERED,
        READ,
        ALL_READ,
        DISMISSED
    }

    private Long notificationId;
    private EventType type;
    private Long userId;
    private LocalDateTime timestamp;
    private Object data;

    public NotificationEvent(Long notificationId, EventType type, Long userId) {
        this.notificationId = notificationId;
        this.type = type;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    // Factory methods for common events

    public static NotificationEvent created(Notification notification) {
        return new NotificationEvent(
                notification.getId(),
                EventType.CREATED,
                notification.getUserId()
        );
    }

    public static NotificationEvent delivered(Notification notification) {
        return new NotificationEvent(
                notification.getId(),
                EventType.DELIVERED,
                notification.getUserId()
        );
    }

    public static NotificationEvent read(Long notificationId, Long userId) {
        return new NotificationEvent(
                notificationId,
                EventType.READ,
                userId
        );
    }
}