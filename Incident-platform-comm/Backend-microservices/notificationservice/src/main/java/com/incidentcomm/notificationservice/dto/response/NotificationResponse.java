package com.incidentcomm.notificationservice.dto.response;

import com.incidentcomm.notificationservice.model.Notification;
import com.incidentcomm.notificationservice.model.NotificationChannel;
import com.incidentcomm.notificationservice.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String username; // Populated from User Service
    private NotificationType type;
    private String title;
    private String message;
    private Long sourceId;
    private String sourceType;
    private String referenceUrl;
    private LocalDateTime createdAt;
    private boolean read;
    private LocalDateTime readAt;
    private boolean delivered;
    private LocalDateTime deliveredAt;
    private NotificationChannel deliveredChannel;
    private boolean dismissed;
    private LocalDateTime dismissedAt;
    private Integer priority;
    private LocalDateTime expiresAt;
    private String actionUrl;
    private String actionText;
    private String iconUrl;
    private boolean silent;

    // Convert from entity to DTO
    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setType(notification.getType());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setSourceId(notification.getSourceId());
        response.setSourceType(notification.getSourceType());
        response.setReferenceUrl(notification.getReferenceUrl());
        response.setCreatedAt(notification.getCreatedAt());
        response.setRead(notification.isRead());
        response.setReadAt(notification.getReadAt());
        response.setDelivered(notification.isDelivered());
        response.setDeliveredAt(notification.getDeliveredAt());
        response.setDeliveredChannel(notification.getDeliveredChannel());
        response.setDismissed(notification.isDismissed());
        response.setDismissedAt(notification.getDismissedAt());
        response.setPriority(notification.getPriority());
        response.setExpiresAt(notification.getExpiresAt());
        response.setActionUrl(notification.getActionUrl());
        response.setActionText(notification.getActionText());
        response.setIconUrl(notification.getIconUrl());
        response.setSilent(notification.isSilent());

        return response;
    }
}