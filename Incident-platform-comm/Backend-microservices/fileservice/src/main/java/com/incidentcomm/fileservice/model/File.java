package com.incidentcomm.fileservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "description")
    private String description;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "incident_id")
    private Long incidentId;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "download_count")
    private Long downloadCount = 0L;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    // Pre-persist hook to set upload time
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Pre-update hook to update the updated_at timestamp
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}