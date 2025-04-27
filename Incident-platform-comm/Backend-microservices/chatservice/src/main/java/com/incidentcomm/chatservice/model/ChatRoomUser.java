package com.incidentcomm.chatservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(name = "user_id")
    private Long userId;

    // Optional nickname for this user in this room
    private String nickname;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Column(name = "is_admin")
    private boolean isAdmin;

    @Column(name = "is_active")
    private boolean isActive = true;

    // Pre-persist hook to set join time
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
        lastReadAt = LocalDateTime.now();
    }
}