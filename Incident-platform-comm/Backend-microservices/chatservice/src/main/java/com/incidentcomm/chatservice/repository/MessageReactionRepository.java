package com.incidentcomm.chatservice.repository;

import com.incidentcomm.chatservice.model.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    // Find reactions for a message
    List<MessageReaction> findByMessageId(Long messageId);

    // Find reaction by message, user and reaction type
    Optional<MessageReaction> findByMessageIdAndUserIdAndReaction(
            Long messageId, Long userId, String reaction);

    // Find all reactions by a user
    List<MessageReaction> findByUserId(Long userId);

    // Count reactions by type for a message
    @Query("SELECT mr.reaction, COUNT(mr) FROM MessageReaction mr " +
            "WHERE mr.messageId = :messageId GROUP BY mr.reaction")
    List<Object[]> countReactionsByType(@Param("messageId") Long messageId);

    // Check if a user has reacted to a message
    boolean existsByMessageIdAndUserId(Long messageId, Long userId);

    // Find all reactions in a chat room
    @Query("SELECT mr FROM MessageReaction mr " +
            "JOIN Message m ON mr.messageId = m.id " +
            "WHERE m.chatRoomId = :chatRoomId")
    List<MessageReaction> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    // Delete a reaction
    void deleteByMessageIdAndUserIdAndReaction(Long messageId, Long userId, String reaction);

    // Get most used reactions in a chat room
    @Query("SELECT mr.reaction, COUNT(mr) as reactionCount FROM MessageReaction mr " +
            "JOIN Message m ON mr.messageId = m.id " +
            "WHERE m.chatRoomId = :chatRoomId " +
            "GROUP BY mr.reaction ORDER BY reactionCount DESC")
    List<Object[]> findMostUsedReactionsInRoom(@Param("chatRoomId") Long chatRoomId);
}