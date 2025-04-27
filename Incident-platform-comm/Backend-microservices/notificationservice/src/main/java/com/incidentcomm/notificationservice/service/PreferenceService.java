package com.incidentcomm.notificationservice.service;

import com.incidentcomm.notificationservice.model.NotificationChannel;
import com.incidentcomm.notificationservice.model.NotificationPreference;
import com.incidentcomm.notificationservice.model.NotificationType;
import com.incidentcomm.notificationservice.repository.NotificationPreferenceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PreferenceService {

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Value("${app.notification.default-preferences}")
    private boolean createDefaultPreferences;

    /**
     * Initialize default preferences for a new user
     */
    @Transactional
    public void initializeDefaultPreferences(Long userId) {
        if (!createDefaultPreferences) {
            return;
        }

        log.info("Creating default notification preferences for user {}", userId);

        // Default preferences for different notification types
        List<NotificationPreference> defaults = new ArrayList<>();

        // Incident notifications - all channels
        for (NotificationType type : List.of(
                NotificationType.INCIDENT_CREATED,
                NotificationType.INCIDENT_ASSIGNED,
                NotificationType.INCIDENT_STATUS_CHANGED,
                NotificationType.INCIDENT_RESOLVED)) {

            defaults.add(new NotificationPreference(userId, type, NotificationChannel.IN_APP, true));
            defaults.add(new NotificationPreference(userId, type, NotificationChannel.WEBSOCKET, true));
            defaults.add(new NotificationPreference(userId, type, NotificationChannel.EMAIL, true));
        }

        // Chat notifications - in-app and websocket only
        for (NotificationType type : List.of(
                NotificationType.CHAT_MESSAGE,
                NotificationType.CHAT_MENTION)) {

            defaults.add(new NotificationPreference(userId, type, NotificationChannel.IN_APP, true));
            defaults.add(new NotificationPreference(userId, type, NotificationChannel.WEBSOCKET, true));
        }

        // File notifications - in-app only
        for (NotificationType type : List.of(
                NotificationType.FILE_UPLOADED,
                NotificationType.FILE_SHARED)) {

            defaults.add(new NotificationPreference(userId, type, NotificationChannel.IN_APP, true));
        }

        // Save all defaults
        preferenceRepository.saveAll(defaults);
        log.info("Created {} default preferences for user {}", defaults.size(), userId);
    }

    /**
     * Get all preferences for a user
     */
    public List<NotificationPreference> getUserPreferences(Long userId) {
        return preferenceRepository.findByUserId(userId);
    }

    /**
     * Update a preference
     */
    @Transactional
    public NotificationPreference updatePreference(Long userId,
                                                   NotificationType type,
                                                   NotificationChannel channel,
                                                   boolean enabled) {
        Optional<NotificationPreference> existing = preferenceRepository
                .findByUserIdAndNotificationTypeAndChannel(userId, type, channel);

        NotificationPreference preference;
        if (existing.isPresent()) {
            preference = existing.get();
            preference.setEnabled(enabled);
        } else {
            preference = new NotificationPreference(userId, type, channel, enabled);
        }

        return preferenceRepository.save(preference);
    }

    /**
     * Update quiet hours for a user
     */
    @Transactional
    public void updateQuietHours(Long userId, Integer startHour, Integer endHour) {
        // Validate hours (0-23)
        if (startHour != null && (startHour < 0 || startHour > 23)) {
            throw new IllegalArgumentException("Invalid start hour");
        }
        if (endHour != null && (endHour < 0 || endHour > 23)) {
            throw new IllegalArgumentException("Invalid end hour");
        }

        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);
        for (NotificationPreference pref : preferences) {
            pref.setQuietHoursStart(startHour);
            pref.setQuietHoursEnd(endHour);
        }

        preferenceRepository.saveAll(preferences);
        log.info("Updated quiet hours for user {}: {}-{}", userId, startHour, endHour);
    }

    /**
     * Update delivery address for email/SMS channels
     */
    @Transactional
    public void updateDeliveryAddress(Long userId, NotificationChannel channel, String address) {
        if (channel != NotificationChannel.EMAIL && channel != NotificationChannel.SMS) {
            throw new IllegalArgumentException("Delivery address only applicable for EMAIL and SMS channels");
        }

        List<NotificationPreference> preferences = preferenceRepository.findByUserIdAndChannel(userId, channel);
        for (NotificationPreference pref : preferences) {
            pref.setDeliveryAddress(address);
        }

        preferenceRepository.saveAll(preferences);
        log.info("Updated delivery address for user {} channel {}: {}", userId, channel, address);
    }

    /**
     * Check if a specific notification type is enabled for a channel
     */
    public boolean isNotificationEnabled(Long userId, NotificationType type, NotificationChannel channel) {
        return preferenceRepository.existsByUserIdAndNotificationTypeAndChannelAndIsEnabled(
                userId, type, channel, true);
    }

    /**
     * Get all users who have enabled a specific notification type on a channel
     */
    public List<Long> getUsersWithPreferenceEnabled(NotificationType type, NotificationChannel channel) {
        return preferenceRepository.findUserIdsWithPreferenceEnabled(type, channel);
    }

    /**
     * Reset preferences to defaults
     */
    @Transactional
    public void resetToDefaults(Long userId) {
        preferenceRepository.deleteByUserId(userId);
        initializeDefaultPreferences(userId);
    }
}