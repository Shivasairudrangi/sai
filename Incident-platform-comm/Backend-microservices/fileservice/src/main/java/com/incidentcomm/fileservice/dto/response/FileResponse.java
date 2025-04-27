package com.incidentcomm.fileservice.dto.response;

import com.incidentcomm.fileservice.model.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private Long id;
    private String fileName;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String description;
    private Long uploadedBy;
    private String uploaderName; // Will be populated from User Service
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    private Long incidentId;
    private String incidentTitle; // Will be populated from Incident Service
    private Long messageId;
    private boolean isPublic;
    private Long downloadCount;
    private String downloadUrl;
    private List<String> tags = new ArrayList<>();
    private List<FilePermissionResponse> permissions = new ArrayList<>();

    // Convert File entity to FileResponse DTO
    public static FileResponse fromEntity(File file) {
        FileResponse response = new FileResponse();
        response.setId(file.getId());
        response.setFileName(file.getFileName());
        response.setOriginalFileName(file.getOriginalFileName());
        response.setFileType(file.getFileType());
        response.setFileSize(file.getFileSize());
        response.setDescription(file.getDescription());
        response.setUploadedBy(file.getUploadedBy());
        response.setUploadedAt(file.getUploadedAt());
        response.setUpdatedAt(file.getUpdatedAt());
        response.setIncidentId(file.getIncidentId());
        response.setMessageId(file.getMessageId());
        response.setPublic(file.isPublic());
        response.setDownloadCount(file.getDownloadCount());

        // Download URL will be set by the service

        return response;
    }

    // Inner class for permissions
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilePermissionResponse {
        private Long userId;
        private String username;
        private String role;
        private boolean canView;
        private boolean canDownload;
        private boolean canEdit;
        private boolean canDelete;
        private LocalDateTime grantedAt;
        private LocalDateTime expiresAt;
    }
}