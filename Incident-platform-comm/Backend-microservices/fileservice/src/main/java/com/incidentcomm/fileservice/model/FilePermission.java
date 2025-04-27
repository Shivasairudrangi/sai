package com.incidentcomm.fileservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "role")
    private String role;

    @Column(name = "can_view")
    private boolean canView = false;

    @Column(name = "can_download")
    private boolean canDownload = false;

    @Column(name = "can_edit")
    private boolean canEdit = false;

    @Column(name = "can_delete")
    private boolean canDelete = false;

    @Column(name = "granted_by", nullable = false)
    private Long grantedBy;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    private boolean isActive = true;

    // Pre-persist hook to set grant time
    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
    }

    // Constructor for user-specific permissions
    public FilePermission(Long fileId, Long userId, Long grantedBy) {
        this.fileId = fileId;
        this.userId = userId;
        this.grantedBy = grantedBy;
    }

    // Constructor for role-based permissions
    public FilePermission(Long fileId, String role, Long grantedBy) {
        this.fileId = fileId;
        this.role = role;
        this.grantedBy = grantedBy;
    }
}