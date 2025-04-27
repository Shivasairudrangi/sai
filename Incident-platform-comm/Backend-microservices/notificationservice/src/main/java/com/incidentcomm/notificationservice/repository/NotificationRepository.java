package com.incidentcomm.notificationservice.repository;

import com.incidentcomm.notificationservice.model.Notification;
import com.incidentcomm.notificationservice.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find notifications for a user
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Find unread notifications for a user
    Page<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, boolean isRead, Pageable pageable);

    // Find notifications by type for a user
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type, Pageable pageable);

    // Find notifications for a specific source (e.g., incident, message)
    List<Notification> findBySourceIdAndSourceType(Long sourceId, String sourceType);

    // Count unread notifications for a user
    long countByUserIdAndIsRead(Long userId, boolean isRead);

    // Mark all notifications as read for a user
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Mark specific notifications as read
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.id IN :ids AND n.userId = :userId")
    int markAsRead(@Param("ids") List<Long> notificationIds, @Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Mark notifications as delivered
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isDelivered = true, n.deliveredAt = :now WHERE n.id IN :ids")
    int markAsDelivered(@Param("ids") List<Long> notificationIds, @Param("now") LocalDateTime now);

    // Dismiss notifications
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isDismissed = true, n.dismissedAt = :now WHERE n.id IN :ids AND n.userId = :userId")
    int dismissNotifications(@Param("ids") List<Long> notificationIds, @Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Delete old notifications
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :before OR (n.expiresAt IS NOT NULL AND n.expiresAt < :now)")
    int deleteOldNotifications(@Param("before") LocalDateTime before, @Param("now") LocalDateTime now);

    // Find notifications that need to be delivered via a specific channel
    @Query("SELECT n FROM Notification n WHERE n.isDelivered = false AND n.userId IN " +
            "(SELECT p.userId FROM NotificationPreference p WHERE p.notificationType = n.type " +
            "AND p.channel = :channel AND p.isEnabled = true) ORDER BY n.priority, n.createdAt")
    List<Notification> findPendingNotificationsForChannel(@Param("channel") String channel, Pageable pageable);

    // Get notification summary counts by type for a user
    @Query("SELECT n.type, COUNT(n) FROM Notification n " +
            "WHERE n.userId = :userId AND n.isRead = false " +
            "GROUP BY n.type ORDER BY COUNT(n) DESC")
    List<Object[]> countUnreadByType(@Param("userId") Long userId);
}