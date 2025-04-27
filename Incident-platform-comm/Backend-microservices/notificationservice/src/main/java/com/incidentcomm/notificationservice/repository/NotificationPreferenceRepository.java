package com.incidentcomm.notificationservice.repository;

import com.incidentcomm.notificationservice.model.NotificationChannel;
import com.incidentcomm.notificationservice.model.NotificationPreference;
import com.incidentcomm.notificationservice.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    // Find all preferences for a user
    List<NotificationPreference> findByUserId(Long userId);

    // Find preferences for a specific notification type and user
    List<NotificationPreference> findByUserIdAndNotificationType(Long userId, NotificationType notificationType);

    // Find a specific preference by user, type, and channel
    Optional<NotificationPreference> findByUserIdAndNotificationTypeAndChannel(
            Long userId, NotificationType notificationType, NotificationChannel channel);

    // Find all enabled preferences for a user
    List<NotificationPreference> findByUserIdAndIsEnabled(Long userId, boolean isEnabled);

    // Find all preferences for a specific channel
    List<NotificationPreference> findByUserIdAndChannel(Long userId, NotificationChannel channel);

    // Check if a specific notification type is enabled for a channel
    boolean existsByUserIdAndNotificationTypeAndChannelAndIsEnabled(
            Long userId, NotificationType notificationType, NotificationChannel channel, boolean isEnabled);

    // Get all users who have enabled a specific notification type on a channel
    @Query("SELECT p.userId FROM NotificationPreference p " +
            "WHERE p.notificationType = :type AND p.channel = :channel AND p.isEnabled = true")
    List<Long> findUserIdsWithPreferenceEnabled(
            @Param("type") NotificationType type, @Param("channel") NotificationChannel channel);

    // Find preferences with delivery address for a channel
    @Query("SELECT p FROM NotificationPreference p " +
            "WHERE p.channel = :channel AND p.isEnabled = true " +
            "AND p.deliveryAddress IS NOT NULL AND LENGTH(p.deliveryAddress) > 0")
    List<NotificationPreference> findPreferencesWithDeliveryAddress(@Param("channel") NotificationChannel channel);

    // Get all notification types enabled for a user on a specific channel
    @Query("SELECT p.notificationType FROM NotificationPreference p " +
            "WHERE p.userId = :userId AND p.channel = :channel AND p.isEnabled = true")
    List<NotificationType> findEnabledNotificationTypes(
            @Param("userId") Long userId, @Param("channel") NotificationChannel channel);

    // Delete all preferences for a user
    void deleteByUserId(Long userId);
}