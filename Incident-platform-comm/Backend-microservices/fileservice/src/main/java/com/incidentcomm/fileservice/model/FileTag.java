package com.incidentcomm.fileservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Pre-persist hook to set creation time
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructor with essential fields
    public FileTag(Long fileId, String tagName, Long createdBy) {
        this.fileId = fileId;
        this.tagName = tagName;
        this.createdBy = createdBy;
    }
}