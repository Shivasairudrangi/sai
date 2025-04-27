package com.incidentcomm.chatservice.repository;

import com.incidentcomm.chatservice.model.Message;
import com.incidentcomm.chatservice.model.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find messages in a chat room with pagination
    Page<Message> findByChatRoomIdAndIsDeletedOrderBySentAtDesc(
            Long chatRoomId, boolean isDeleted, Pageable pageable);

    // Find messages by sender
    List<Message> findBySenderIdAndIsDeleted(Long senderId, boolean isDeleted);

    // Find messages by type
    List<Message> findByChatRoomIdAndTypeAndIsDeleted(
            Long chatRoomId, MessageType type, boolean isDeleted);

    // Find messages by content containing keyword
    @Query("SELECT m FROM Message m WHERE m.chatRoomId = :chatRoomId " +
            "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND m.isDeleted = false ORDER BY m.sentAt DESC")
    Page<Message> searchMessages(
            @Param("chatRoomId") Long chatRoomId,
            @Param("keyword") String keyword,
            Pageable pageable);

    // Find recent messages in a chat room
    List<Message> findTop20ByChatRoomIdAndIsDeletedOrderBySentAtDesc(
            Long chatRoomId, boolean isDeleted);

    // Count unread messages in a chat room for a user
    @Query("SELECT COUNT(m) FROM Message m " +
            "LEFT JOIN MessageStatus ms ON m.id = ms.messageId AND ms.userId = :userId " +
            "WHERE m.chatRoomId = :chatRoomId " +
            "AND m.senderId <> :userId " +
            "AND m.isDeleted = false " +
            "AND (ms.id IS NULL OR ms.isRead = false)")
    long countUnreadMessages(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId);

    // Find messages with a specific parent (replies)
    List<Message> findByParentMessageIdAndIsDeleted(Long parentMessageId, boolean isDeleted);

    // Find messages within a date range
    List<Message> findByChatRoomIdAndSentAtBetweenAndIsDeleted(
            Long chatRoomId, LocalDateTime startDate, LocalDateTime endDate, boolean isDeleted);

    // Find messages with file attachments
    @Query("SELECT m FROM Message m WHERE m.chatRoomId = :chatRoomId " +
            "AND m.fileUrl IS NOT NULL " +
            "AND m.isDeleted = false ORDER BY m.sentAt DESC")
    Page<Message> findFileAttachments(
            @Param("chatRoomId") Long chatRoomId,
            Pageable pageable);

    // Find messages with file attachments of specific type
    @Query("SELECT m FROM Message m WHERE m.chatRoomId = :chatRoomId " +
            "AND m.fileUrl IS NOT NULL " +
            "AND LOWER(m.fileType) LIKE LOWER(CONCAT('%', :fileType, '%')) " +
            "AND m.isDeleted = false ORDER BY m.sentAt DESC")
    Page<Message> findFileAttachmentsByType(
            @Param("chatRoomId") Long chatRoomId,
            @Param("fileType") String fileType,
            Pageable pageable);
}