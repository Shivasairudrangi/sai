package com.incidentcomm.chatservice.repository;

import com.incidentcomm.chatservice.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // Find a chat room by name
    Optional<ChatRoom> findByName(String name);

    // Find chat rooms by creator
    List<ChatRoom> findByCreatedBy(Long userId);

    // Find chat rooms related to an incident
    List<ChatRoom> findByIncidentId(Long incidentId);

    // Find chat rooms by whether they are groups or not
    List<ChatRoom> findByIsGroup(boolean isGroup);

    // Find active chat rooms
    List<ChatRoom> findByIsActive(boolean isActive);

    // Find chat rooms containing a specific user
    @Query("SELECT cr FROM ChatRoom cr JOIN ChatRoomUser cru ON cr.id = cru.chatRoomId " +
            "WHERE cru.userId = :userId AND cru.isActive = true AND cr.isActive = true")
    List<ChatRoom> findByUserId(@Param("userId") Long userId);

    // Find direct chat room between two users
    @Query("SELECT cr FROM ChatRoom cr " +
            "JOIN ChatRoomUser cru1 ON cr.id = cru1.chatRoomId " +
            "JOIN ChatRoomUser cru2 ON cr.id = cru2.chatRoomId " +
            "WHERE cr.isGroup = false " +
            "AND cru1.userId = :user1Id " +
            "AND cru2.userId = :user2Id " +
            "AND cr.isActive = true")
    Optional<ChatRoom> findDirectChatBetweenUsers(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id);

    // Find or create a chat room for an incident
    Optional<ChatRoom> findByIncidentIdAndIsIncidentRelated(Long incidentId, boolean isIncidentRelated);
}