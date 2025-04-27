package com.incidentcomm.chatservice.repository;

import com.incidentcomm.chatservice.model.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, Long> {

    // Find status for a specific message and user
    Optional<MessageStatus> findByMessageIdAndUserId(Long messageId, Long userId);

    // Find statuses for a message
    List<MessageStatus> findByMessageId(Long messageId);

    // Get all message statuses for a user
    List<MessageStatus> findByUserId(Long userId);

    // Mark messages as delivered for a user
    @Modifying
    @Transactional
    @Query("UPDATE MessageStatus ms SET ms.isDelivered = true, ms.deliveredAt = :now " +
            "WHERE ms.userId = :userId AND ms.messageId IN :messageIds AND ms.isDelivered = false")
    int markMessagesAsDelivered(
            @Param("userId") Long userId,
            @Param("messageIds") List<Long> messageIds,
            @Param("now") LocalDateTime now);

    // Mark messages as read for a user
    @Modifying
    @Transactional
    @Query("UPDATE MessageStatus ms SET ms.isRead = true, ms.readAt = :now, " +
            "ms.isDelivered = true, ms.deliveredAt = CASE WHEN ms.deliveredAt IS NULL THEN :now ELSE ms.deliveredAt END " +
            "WHERE ms.userId = :userId AND ms.messageId IN :messageIds AND ms.isRead = false")
    int markMessagesAsRead(
            @Param("userId") Long userId,
            @Param("messageIds") List<Long> messageIds,
            @Param("now") LocalDateTime now);

    // Get unread message count for a user in a chat room
    @Query("SELECT COUNT(ms) FROM MessageStatus ms " +
            "JOIN Message m ON ms.messageId = m.id " +
            "WHERE ms.userId = :userId AND m.chatRoomId = :chatRoomId " +
            "AND ms.isRead = false")
    long countUnreadMessagesForUserInRoom(
            @Param("userId") Long userId,
            @Param("chatRoomId") Long chatRoomId);

    // Check if all users have read a message
    @Query("SELECT COUNT(cru) = COUNT(ms.id) " +
            "FROM ChatRoomUser cru " +
            "LEFT JOIN MessageStatus ms ON cru.userId = ms.userId AND ms.messageId = :messageId AND ms.isRead = true " +
            "WHERE cru.chatRoomId = (SELECT m.chatRoomId FROM Message m WHERE m.id = :messageId) " +
            "AND cru.isActive = true")
    boolean isMessageReadByAllUsers(@Param("messageId") Long messageId);
}