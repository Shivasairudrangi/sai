package com.incidentcomm.chatservice.repository;

import com.incidentcomm.chatservice.model.ChatRoomUser;
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
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

    // Find all users in a specific chat room
    List<ChatRoomUser> findByChatRoomId(Long chatRoomId);

    // Find active users in a chat room
    List<ChatRoomUser> findByChatRoomIdAndIsActive(Long chatRoomId, boolean isActive);

    // Find all chat rooms for a user
    List<ChatRoomUser> findByUserIdAndIsActive(Long userId, boolean isActive);

    // Find if a user is in a specific chat room
    Optional<ChatRoomUser> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    // Find admin users of a chat room
    List<ChatRoomUser> findByChatRoomIdAndIsAdmin(Long chatRoomId, boolean isAdmin);

    // Update lastReadAt for a user in a chat room
    @Modifying
    @Transactional
    @Query("UPDATE ChatRoomUser cru SET cru.lastReadAt = :lastReadAt " +
            "WHERE cru.chatRoomId = :chatRoomId AND cru.userId = :userId")
    void updateLastReadAt(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId,
            @Param("lastReadAt") LocalDateTime lastReadAt);

    // Remove a user from a chat room (mark as inactive)
    @Modifying
    @Transactional
    @Query("UPDATE ChatRoomUser cru SET cru.isActive = false " +
            "WHERE cru.chatRoomId = :chatRoomId AND cru.userId = :userId")
    void removeUserFromChatRoom(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId);

    // Check if a user is an admin of a chat room
    boolean existsByChatRoomIdAndUserIdAndIsAdmin(
            Long chatRoomId, Long userId, boolean isAdmin);

    // Count the number of active users in a chat room
    long countByChatRoomIdAndIsActive(Long chatRoomId, boolean isActive);
}