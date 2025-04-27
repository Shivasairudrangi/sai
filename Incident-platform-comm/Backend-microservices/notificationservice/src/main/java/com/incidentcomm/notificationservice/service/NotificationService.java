package com.incidentcomm.notificationservice.service;

import com.incidentcomm.notificationservice.dto.request.NotificationRequest;
import com.incidentcomm.notificationservice.dto.response.NotificationResponse;
import com.incidentcomm.notificationservice.event.NotificationEvent;
import com.incidentcomm.notificationservice.model.Notification;
import com.incidentcomm.notificationservice.model.NotificationChannel;
import com.incidentcomm.notificationservice.model.NotificationPreference;
import com.incidentcomm.notificationservice.model.NotificationType;
import com.incidentcomm.notificationservice.repository.NotificationPreferenceRepository;
import com.incidentcomm.notificationservice.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.service.user}")
    private String userServiceUrl;

    @Value("${app.notification.cleanup-days}")
    private int cleanupDays;

    /**
     * Create a notification for a single user
     */
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        if (request.getUserId() != null) {
            Notification notification = createNotificationEntity(request.getUserId(), request);
            return sendNotification(notification);
        } else if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            // Create notifications for multiple users
            List<NotificationResponse> responses = new ArrayList<>();
            for (Long userId : request.getUserIds()) {
                responses.add(sendNotification(createNotificationEntity(userId, request)));
            }
            // Return the first response (for simplicity)
            return responses.isEmpty() ? null : responses.get(0);
        } else if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            // Get users by role and create notifications
            List<Long> userIds = getUsersByRoles(request.getRoles());
            List<NotificationResponse> responses = new ArrayList<>();
            for (Long userId : userIds) {
                responses.add(sendNotification(createNotificationEntity(userId, request)));
            }
            // Return the first response (for simplicity)
            return responses.isEmpty() ? null : responses.get(0);
        }

        throw new IllegalArgumentException("Must specify userId, userIds, or roles");
    }

    /**
     * Create and send a notification
     */
    private NotificationResponse sendNotification(Notification notification) {
        Notification savedNotification = notificationRepository.save(notification);

        // Publish creation event
        eventPublisher.publishEvent(NotificationEvent.created(savedNotification));

        // Route to appropriate channels based on user preferences
        List<NotificationPreference> preferences = preferenceRepository
                .findByUserIdAndNotificationType(notification.getUserId(), notification.getType());

        for (NotificationPreference pref : preferences) {
            if (pref.isEnabled() && pref.shouldSendNow()) {
                channelService.sendViaChannel(notification, pref.getChannel());
            }
        }

        NotificationResponse response = NotificationResponse.fromEntity(savedNotification);
        response.setUsername(getUsernameById(savedNotification.getUserId()));
        return response;
    }

    /**
     * Get all notifications for a user
     */
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return notifications.map(notification -> {
            NotificationResponse response = NotificationResponse.fromEntity(notification);
            response.setUsername(getUsernameById(userId));
            return response;
        });
    }

    /**
     * Get unread notifications for a user
     */
    public Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable);

        return notifications.map(notification -> {
            NotificationResponse response = NotificationResponse.fromEntity(notification);
            response.setUsername(getUsernameById(userId));
            return response;
        });
    }

    /**
     * Mark notifications as read
     */
    @Transactional
    public void markAsRead(List<Long> notificationIds, Long userId) {
        int updated = notificationRepository.markAsRead(notificationIds, userId, LocalDateTime.now());
        log.info("Marked {} notifications as read for user {}", updated, userId);

        // Publish read events
        notificationIds.forEach(id -> eventPublisher.publishEvent(
                new NotificationEvent(id, NotificationEvent.EventType.READ, userId)));
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        log.info("Marked {} notifications as read for user {}", updated, userId);

        // Publish read event
        eventPublisher.publishEvent(new NotificationEvent(null, NotificationEvent.EventType.ALL_READ, userId));
    }

    /**
     * Dismiss notifications
     */
    @Transactional
    public void dismissNotifications(List<Long> notificationIds, Long userId) {
        int updated = notificationRepository.dismissNotifications(notificationIds, userId, LocalDateTime.now());
        log.info("Dismissed {} notifications for user {}", updated, userId);

        // Publish dismiss events
        notificationIds.forEach(id -> eventPublisher.publishEvent(
                new NotificationEvent(id, NotificationEvent.EventType.DISMISSED, userId)));
    }

    /**
     * Get notification count by type for a user
     */
    public List<Object[]> getNotificationSummary(Long userId) {
        return notificationRepository.countUnreadByType(userId);
    }

    /**
     * Clean up old notifications
     */
    @Scheduled(cron = "0 0 1 * * ?") // Run at 1 AM every day
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime before = LocalDateTime.now().minusDays(cleanupDays);
        int deleted = notificationRepository.deleteOldNotifications(before, LocalDateTime.now());
        log.info("Deleted {} old notifications", deleted);
    }

    /**
     * Create a notification entity from request
     */
    private Notification createNotificationEntity(Long userId, NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setSourceId(request.getSourceId());
        notification.setSourceType(request.getSourceType());
        notification.setReferenceUrl(request.getReferenceUrl());
        notification.setActionUrl(request.getActionUrl());
        notification.setActionText(request.getActionText());
        notification.setIconUrl(request.getIconUrl());
        notification.setPriority(request.getPriority());
        notification.setExpiresAt(request.getExpiresAt());
        notification.setSilent(request.isSilent());

        return notification;
    }

    /**
     * Get user IDs by roles
     */
    private List<Long> getUsersByRoles(List<String> roles) {
        // In a real implementation, this would call the User Service
        // Mock implementation for now
        log.info("Getting users with roles: {}", roles);
        return List.of(1L, 2L, 3L); // Mock data
    }

    /**
     * Get username by user ID
     */
    private String getUsernameById(Long userId) {
        // In a real implementation, this would call the User Service
        // Mock implementation for now
        return "User " + userId;
    }
}