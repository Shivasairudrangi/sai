package com.incidentcomm.notificationservice.dto.request;

import com.incidentcomm.notificationservice.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    // Single recipient
    private Long userId;

    // Multiple recipients
    private List<Long> userIds;

    // Role-based recipients
    private List<String> roles;

    @NotNull
    private NotificationType type;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    // Optional link to associated entity
    private Long sourceId;
    private String sourceType;

    // Optional URL to view the source
    private String referenceUrl;

    // Optional custom action
    private String actionUrl;
    private String actionText;

    // Optional icon URL
    private String iconUrl;

    // Priority (1 = highest, 5 = lowest)
    private Integer priority = 3;

    // Optional expiration time
    private LocalDateTime expiresAt;

    // Silent notification (no sounds/alerts)
    private boolean silent = false;
}