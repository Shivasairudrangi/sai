package com.incidentcomm.chatservice.repository;

import com.incidentcomm.chatservice.model.MessageMention;
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
public interface MessageMentionRepository extends JpaRepository<MessageMention, Long> {

    // Find mentions in a message
    List<MessageMention> findByMessageId(Long messageId);

    // Find mention by message id and mentioned user id
    Optional<MessageMention> findByMessageIdAndMentionedUserId(Long messageId, Long mentionedUserId);

    // Find all mentions for a user
    List<MessageMention> findByMentionedUserId(Long mentionedUserId);

    // Find unread mentions for a user
    List<MessageMention> findByMentionedUserIdAndIsRead(Long mentionedUserId, boolean isRead);

    // Count unread mentions for a user
    long countByMentionedUserIdAndIsRead(Long mentionedUserId, boolean isRead);

    // Mark mentions as read
    @Modifying
    @Transactional
    @Query("UPDATE MessageMention mm SET mm.isRead = true, mm.readAt = :now " +
            "WHERE mm.mentionedUserId = :userId AND mm.isRead = false")
    int markAllMentionsAsRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Mark specific mentions as read
    @Modifying
    @Transactional
    @Query("UPDATE MessageMention mm SET mm.isRead = true, mm.readAt = :now " +
            "WHERE mm.id IN :mentionIds AND mm.mentionedUserId = :userId AND mm.isRead = false")
    int markMentionsAsRead(
            @Param("mentionIds") List<Long> mentionIds,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    // Get mentions in a chat room for a user
    @Query("SELECT mm FROM MessageMention mm " +
            "JOIN Message m ON mm.messageId = m.id " +
            "WHERE m.chatRoomId = :chatRoomId AND mm.mentionedUserId = :userId")
    List<MessageMention> findByChatRoomIdAndMentionedUserId(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId);

    // Find unread mentions in a chat room for a user
    @Query("SELECT mm FROM MessageMention mm " +
            "JOIN Message m ON mm.messageId = m.id " +
            "WHERE m.chatRoomId = :chatRoomId AND mm.mentionedUserId = :userId AND mm.isRead = false")
    List<MessageMention> findUnreadByChatRoomIdAndMentionedUserId(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId);
}