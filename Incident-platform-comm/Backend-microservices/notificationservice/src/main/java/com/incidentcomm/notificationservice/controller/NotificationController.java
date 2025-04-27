package com.incidentcomm.notificationservice.controller;

import com.incidentcomm.notificationservice.dto.request.NotificationRequest;
import com.incidentcomm.notificationservice.dto.response.NotificationResponse;
import com.incidentcomm.notificationservice.model.NotificationChannel;
import com.incidentcomm.notificationservice.model.NotificationPreference;
import com.incidentcomm.notificationservice.model.NotificationType;
import com.incidentcomm.notificationservice.service.NotificationService;
import com.incidentcomm.notificationservice.service.PreferenceService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PreferenceService preferenceService;

    /**
     * Create a new notification
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request) {

        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all notifications for the current user
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader("X-User-ID") Long userId) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);

        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications for the current user
     */
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-ID") Long userId) {

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId, pageable);

        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark notifications as read
     */
    @PostMapping("/mark-read")
    public ResponseEntity<Void> markNotificationsAsRead(
            @RequestBody List<Long> notificationIds,
            @RequestHeader("X-User-ID") Long userId) {

        notificationService.markAsRead(notificationIds, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all notifications as read
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllNotificationsAsRead(
            @RequestHeader("X-User-ID") Long userId) {

        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Dismiss notifications
     */
    @PostMapping("/dismiss")
    public ResponseEntity<Void> dismissNotifications(
            @RequestBody List<Long> notificationIds,
            @RequestHeader("X-User-ID") Long userId) {

        notificationService.dismissNotifications(notificationIds, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get notification summary for current user
     */
    @GetMapping("/summary")
    public ResponseEntity<List<Object[]>> getNotificationSummary(
            @RequestHeader("X-User-ID") Long userId) {

        List<Object[]> summary = notificationService.getNotificationSummary(userId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get user preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<List<NotificationPreference>> getUserPreferences(
            @RequestHeader("X-User-ID") Long userId) {

        List<NotificationPreference> preferences = preferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Update a preference
     */
    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreference> updatePreference(
            @RequestParam NotificationType type,
            @RequestParam NotificationChannel channel,
            @RequestParam boolean enabled,
            @RequestHeader("X-User-ID") Long userId) {

        NotificationPreference updated = preferenceService.updatePreference(userId, type, channel, enabled);
        return ResponseEntity.ok(updated);
    }

    /**
     * Update quiet hours
     */
    @PutMapping("/preferences/quiet-hours")
    public ResponseEntity<Void> updateQuietHours(
            @RequestParam(required = false) Integer startHour,
            @RequestParam(required = false) Integer endHour,
            @RequestHeader("X-User-ID") Long userId) {

        preferenceService.updateQuietHours(userId, startHour, endHour);
        return ResponseEntity.ok().build();
    }

    /**
     * Update delivery address for email/SMS
     */
    @PutMapping("/preferences/delivery-address")
    public ResponseEntity<Void> updateDeliveryAddress(
            @RequestParam NotificationChannel channel,
            @RequestParam String address,
            @RequestHeader("X-User-ID") Long userId) {

        preferenceService.updateDeliveryAddress(userId, channel, address);
        return ResponseEntity.ok().build();
    }

    /**
     * Reset preferences to defaults
     */
    @PostMapping("/preferences/reset")
    public ResponseEntity<Void> resetPreferences(
            @RequestHeader("X-User-ID") Long userId) {

        preferenceService.resetToDefaults(userId);
        return ResponseEntity.ok().build();
    }
}