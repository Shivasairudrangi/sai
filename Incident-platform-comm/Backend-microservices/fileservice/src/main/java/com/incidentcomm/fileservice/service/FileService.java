package com.incidentcomm.fileservice.service;

import com.incidentcomm.fileservice.dto.request.FileTagRequest;
import com.incidentcomm.fileservice.dto.response.FileResponse;
import com.incidentcomm.fileservice.dto.response.UploadResponse;
import com.incidentcomm.fileservice.event.FileEvent;
import com.incidentcomm.fileservice.exception.FileNotFoundException;
import com.incidentcomm.fileservice.exception.FileStorageException;
import com.incidentcomm.fileservice.model.File;
import com.incidentcomm.fileservice.model.FilePermission;
import com.incidentcomm.fileservice.model.FileTag;
import com.incidentcomm.fileservice.repository.FilePermissionRepository;
import com.incidentcomm.fileservice.repository.FileRepository;
import com.incidentcomm.fileservice.repository.FileTagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileTagRepository fileTagRepository;

    @Autowired
    private FilePermissionRepository filePermissionRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Upload a new file
     */
    @Transactional
    public UploadResponse uploadFile(MultipartFile file, String description,
                                     Long incidentId, Long messageId,
                                     boolean isPublic, Long userId,
                                     List<String> tags) {
        try {
            // Store file in storage system
            String fileName = storageService.storeFile(file);

            // Create file record in database
            File fileEntity = new File();
            fileEntity.setFileName(fileName);
            fileEntity.setOriginalFileName(file.getOriginalFilename());
            fileEntity.setFilePath(storageService.getFileStorageLocation().resolve(fileName).toString());
            fileEntity.setFileType(file.getContentType());
            fileEntity.setFileSize(file.getSize());
            fileEntity.setDescription(description);
            fileEntity.setUploadedBy(userId);
            fileEntity.setIncidentId(incidentId);
            fileEntity.setMessageId(messageId);
            fileEntity.setPublic(isPublic);

            File savedFile = fileRepository.save(fileEntity);

            // Add tags if provided
            if (tags != null && !tags.isEmpty()) {
                for (String tag : tags) {
                    fileTagRepository.save(new FileTag(savedFile.getId(), tag, userId));
                }
            }

            // Create default permissions
            createDefaultPermissions(savedFile.getId(), userId, isPublic);

            // Generate download URL
            String downloadUrl = generateDownloadUrl(fileName);

            // Publish file uploaded event
            publishFileUploadedEvent(savedFile, userId);

            return UploadResponse.success(
                    fileName,
                    fileEntity.getOriginalFileName(),
                    fileEntity.getFileType(),
                    fileEntity.getFileSize(),
                    downloadUrl,
                    savedFile.getId()
            );
        } catch (Exception e) {
            log.error("Failed to upload file", e);
            return UploadResponse.failure(
                    file.getOriginalFilename(),
                    e.getMessage()
            );
        }
    }

    /**
     * Get file by ID
     */
    public FileResponse getFileById(Long fileId, Long userId, List<String> userRoles) {
        File file = fileRepository.findByIdAndIsDeleted(fileId, false)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));

        // Check if user has permission to view the file
        if (!hasAccessToFile(file, userId, userRoles)) {
            throw new AccessDeniedException("You don't have permission to access this file");
        }

        FileResponse response = FileResponse.fromEntity(file);

        // Set download URL
        response.setDownloadUrl(generateDownloadUrl(file.getFileName()));

        // Get uploader name
        response.setUploaderName(getUsernameById(file.getUploadedBy()));

        // Get incident title if applicable
        if (file.getIncidentId() != null) {
            response.setIncidentTitle(getIncidentTitle(file.getIncidentId()));
        }

        // Get tags
        List<FileTag> tags = fileTagRepository.findByFileId(fileId);
        if (!tags.isEmpty()) {
            response.setTags(tags.stream().map(FileTag::getTagName).collect(Collectors.toList()));
        }

        // Get permissions
        List<FilePermission> permissions = filePermissionRepository.findByFileIdAndIsActive(fileId, true);
        if (!permissions.isEmpty()) {
            for (FilePermission permission : permissions) {
                FileResponse.FilePermissionResponse permissionResponse = new FileResponse.FilePermissionResponse();
                permissionResponse.setUserId(permission.getUserId());
                if (permission.getUserId() != null) {
                    permissionResponse.setUsername(getUsernameById(permission.getUserId()));
                }
                permissionResponse.setRole(permission.getRole());
                permissionResponse.setCanView(permission.isCanView());
                permissionResponse.setCanDownload(permission.isCanDownload());
                permissionResponse.setCanEdit(permission.isCanEdit());
                permissionResponse.setCanDelete(permission.isCanDelete());
                permissionResponse.setGrantedAt(permission.getGrantedAt());
                permissionResponse.setExpiresAt(permission.getExpiresAt());

                response.getPermissions().add(permissionResponse);
            }
        }

        return response;
    }

    /**
     * Get all files for an incident
     */
    public List<FileResponse> getFilesByIncidentId(Long incidentId, Long userId, List<String> userRoles) {
        List<File> files = fileRepository.findByIncidentIdAndIsDeletedOrderByUploadedAtDesc(incidentId, false);

        return files.stream()
                .filter(file -> hasAccessToFile(file, userId, userRoles))
                .map(file -> {
                    FileResponse response = FileResponse.fromEntity(file);
                    response.setDownloadUrl(generateDownloadUrl(file.getFileName()));
                    response.setUploaderName(getUsernameById(file.getUploadedBy()));

                    // Get tags
                    List<FileTag> tags = fileTagRepository.findByFileId(file.getId());
                    if (!tags.isEmpty()) {
                        response.setTags(tags.stream().map(FileTag::getTagName).collect(Collectors.toList()));
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all files uploaded by a user
     */
    public Page<FileResponse> getUserFiles(Long userId, List<String> userRoles, Pageable pageable) {
        Page<File> files = fileRepository.findUserFiles(userId, pageable);

        return files.map(file -> {
            FileResponse response = FileResponse.fromEntity(file);
            response.setDownloadUrl(generateDownloadUrl(file.getFileName()));
            response.setUploaderName(getUsernameById(file.getUploadedBy()));

            if (file.getIncidentId() != null) {
                response.setIncidentTitle(getIncidentTitle(file.getIncidentId()));
            }

            // Get tags
            List<FileTag> tags = fileTagRepository.findByFileId(file.getId());
            if (!tags.isEmpty()) {
                response.setTags(tags.stream().map(FileTag::getTagName).collect(Collectors.toList()));
            }

            return response;
        });
    }

    /**
     * Search files
     */
    public Page<FileResponse> searchFiles(String keyword, Long userId, List<String> userRoles, Pageable pageable) {
        Page<File> files = fileRepository.searchFiles(keyword, pageable);

        return files.map(file -> {
            // Check if user has access to the file
            if (!hasAccessToFile(file, userId, userRoles)) {
                return null;
            }

            FileResponse response = FileResponse.fromEntity(file);
            response.setDownloadUrl(generateDownloadUrl(file.getFileName()));
            response.setUploaderName(getUsernameById(file.getUploadedBy()));

            if (file.getIncidentId() != null) {
                response.setIncidentTitle(getIncidentTitle(file.getIncidentId()));
            }

            // Get tags
            List<FileTag> tags = fileTagRepository.findByFileId(file.getId());
            if (!tags.isEmpty()) {
                response.setTags(tags.stream().map(FileTag::getTagName).collect(Collectors.toList()));
            }

            return response;
        }).map(response -> response != null ? response : null);
    }

    /**
     * Download a file
     */
    public Resource downloadFile(String fileName, Long userId, List<String> userRoles) {
        // Find file by name
        List<File> files = fileRepository.findAll().stream()
                .filter(file -> file.getFileName().equals(fileName) && !file.isDeleted())
                .collect(Collectors.toList());

        if (files.isEmpty()) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        File file = files.get(0);

        // Check if user has permission to download
        boolean hasPermission = file.isPublic() || file.getUploadedBy().equals(userId) ||
                filePermissionRepository.hasDownloadPermission(file.getId(), userId, userRoles);

        if (!hasPermission) {
            throw new AccessDeniedException("You don't have permission to download this file");
        }

        // Increment download count
        file.setDownloadCount(file.getDownloadCount() + 1);
        fileRepository.save(file);

        // Publish download event
        eventPublisher.publishEvent(FileEvent.fileDownloaded(file.getId(), fileName, userId));

        return storageService.loadFileAsResource(fileName);
    }

    /**
     * Update file metadata
     */
    @Transactional
    public FileResponse updateFile(Long fileId, String description, boolean isPublic,
                                   Long userId, List<String> userRoles) {
        File file = fileRepository.findByIdAndIsDeleted(fileId, false)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));

        // Check if user has permission to edit
        boolean hasPermission = file.getUploadedBy().equals(userId) ||
                filePermissionRepository.hasEditPermission(fileId, userId, userRoles);

        if (!hasPermission) {
            throw new AccessDeniedException("You don't have permission to edit this file");
        }

        // Update file information
        if (description != null) {
            file.setDescription(description);
        }

        // Update visibility if changed
        if (file.isPublic() != isPublic) {
            file.setPublic(isPublic);

            // Update permissions based on new visibility
            if (isPublic) {
                // Create a public permission for all users
                createPublicPermission(fileId, userId);
            } else {
                // Remove public permission if exists
                removePublicPermissions(fileId);
            }
        }

        File updatedFile = fileRepository.save(file);

        // Publish update event
        eventPublisher.publishEvent(FileEvent.fileUpdated(fileId, file.getFileName(), userId));

        return getFileById(updatedFile.getId(), userId, userRoles);
    }

    /**
     * Delete a file
     */
    @Transactional
    public void deleteFile(Long fileId, Long userId, List<String> userRoles) {
        File file = fileRepository.findByIdAndIsDeleted(fileId, false)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));

        // Check if user has permission to delete
        boolean hasPermission = file.getUploadedBy().equals(userId) ||
                filePermissionRepository.hasDeletePermission(fileId, userId, userRoles);

        if (!hasPermission) {
            throw new AccessDeniedException("You don't have permission to delete this file");
        }

        // Soft delete in the database
        file.setDeleted(true);
        fileRepository.save(file);

        // Publish delete event
        eventPublisher.publishEvent(FileEvent.fileDeleted(fileId, file.getFileName(), userId));

        // Note: We're not deleting the actual file from storage here
        // This could be handled by a scheduled cleanup task for deleted files
    }

    /**
     * Add tags to a file
     */
    @Transactional
    public FileResponse addTags(FileTagRequest request, Long userId, List<String> userRoles) {
        File file = fileRepository.findByIdAndIsDeleted(request.getFileId(), false)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + request.getFileId()));

        // Check if user has permission to edit
        boolean hasPermission = file.getUploadedBy().equals(userId) ||
                filePermissionRepository.hasEditPermission(file.getId(), userId, userRoles);

        if (!hasPermission) {
            throw new AccessDeniedException("You don't have permission to edit this file");
        }

        List<String> tagsToAdd = new ArrayList<>();

        // Handle single tag
        if (request.getTagName() != null && !request.getTagName().isEmpty()) {
            tagsToAdd.add(request.getTagName());
        }

        // Handle multiple tags
        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            tagsToAdd.addAll(request.getTagNames());
        }

        // Add tags
        for (String tag : tagsToAdd) {
            // Check if tag already exists
            if (!fileTagRepository.findByFileIdAndTagName(file.getId(), tag).isPresent()) {
                fileTagRepository.save(new FileTag(file.getId(), tag, userId));

                // Publish tag event
                eventPublisher.publishEvent(FileEvent.tagAdded(file.getId(), file.getFileName(), userId, tag));
            }
        }

        return getFileById(file.getId(), userId, userRoles);
    }

    /**
     * Remove a tag from a file
     */
    @Transactional
    public FileResponse removeTag(Long fileId, String tagName, Long userId, List<String> userRoles) {
        File file = fileRepository.findByIdAndIsDeleted(fileId, false)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));

        // Check if user has permission to edit
        boolean hasPermission = file.getUploadedBy().equals(userId) ||
                filePermissionRepository.hasEditPermission(fileId, userId, userRoles);

        if (!hasPermission) {
            throw new AccessDeniedException("You don't have permission to edit this file");
        }

        // Find the tag
        FileTag tag = fileTagRepository.findByFileIdAndTagName(fileId, tagName)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagName));

        // Remove tag
        fileTagRepository.delete(tag);

        // Publish event
        eventPublisher.publishEvent(FileEvent.tagRemoved(fileId, file.getFileName(), userId, tagName));

        return getFileById(fileId, userId, userRoles);
    }

    /**
     * Share a file with a user
     */
    @Transactional
    public FileResponse shareFileWithUser(Long fileId, Long targetUserId, boolean canView,
                                          boolean canDownload, boolean canEdit, boolean canDelete,
                                          LocalDateTime expiresAt, Long userId, List<String> userRoles) {
        File file = fileRepository.findByIdAndIsDeleted(fileId, false)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));

        // Check if user has permission to share
        boolean hasPermission = file.getUploadedBy().equals(userId) ||
                filePermissionRepository.hasEditPermission(fileId, userId, userRoles);

        if (!hasPermission) {
            throw new AccessDeniedException("You don't have permission to share this file");
        }

        // Check if permission already exists
        FilePermission permission = filePermissionRepository
                .findByFileIdAndUserIdAndIsActive(fileId, targetUserId, true)
                .orElse(new FilePermission(fileId, targetUserId, userId));

        // Update permissions
        permission.setCanView(canView);
        permission.setCanDownload(canDownload);
        permission.setCanEdit(canEdit);
        permission.setCanDelete(canDelete);
        permission.setExpiresAt(expiresAt);
        permission.setActive(true);

        filePermissionRepository.save(permission);

        // Publish share event
        eventPublisher.publishEvent(FileEvent.fileShared(fileId, file.getFileName(), userId, targetUserId));

        return getFileById(fileId, userId, userRoles);
    }

    /**
     * Share a file with a role
     */
    @Transactional
    public FileResponse shareFileWithRole(Long fileId, String role, boolean canView,
                                          boolean canDownload, boolean canEdit, boolean canDelete,
                                          LocalDateTime expiresAt, Long userId, List<String> userRoles) {
        File file = fileRepository.findByIdAndIsDeleted(fileId, false)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));

        // Check if user has permission to share
        boolean hasPermission = file.getUploadedBy().equals(userId) ||
                filePermissionRepository.hasEditPermission(fileId, userId, userRoles);

        if (!hasPermission) {
            throw new AccessDeniedException("You don't have permission to share this file");
        }

        // Check if permission already exists
        FilePermission permission = filePermissionRepository
                .findByFileIdAndRoleAndIsActive(fileId, role, true)
                .orElse(new FilePermission(fileId, role, userId));

        // Update permissions
        permission.setCanView(canView);
        permission.setCanDownload(canDownload);
        permission.setCanEdit(canEdit);
        permission.setCanDelete(canDelete);
        permission.setExpiresAt(expiresAt);
        permission.setActive(true);

        filePermissionRepository.save(permission);

        // Publish permission changed event
        eventPublisher.publishEvent(FileEvent.permissionChanged(fileId, file.getFileName(), userId, role));

        return getFileById(fileId, userId, userRoles);
    }

    /**
     * Remove file sharing permission
     */
    @Transactional
    public FileResponse removeFilePermission(Long fileId, Long permissionId,
                                             Long userId, List<String> userRoles) {
        File file = fileRepository.findByIdAndIsDeleted(fileId, false)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));

        // Check if user has permission to modify sharing
        boolean hasPermission = file.getUploadedBy().equals(userId) ||
                filePermissionRepository.hasEditPermission(fileId, userId, userRoles);

        if (!hasPermission) {
            throw new AccessDeniedException("You don't have permission to modify sharing settings");
        }

        // Find the permission
        FilePermission permission = filePermissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));

        // Ensure the permission belongs to the correct file
        if (!permission.getFileId().equals(fileId)) {
            throw new IllegalArgumentException("Permission does not belong to specified file");
        }

        // Deactivate the permission
        permission.setActive(false);
        filePermissionRepository.save(permission);

        return getFileById(fileId, userId, userRoles);
    }

    /**
     * Find files by tag
     */
    public List<FileResponse> findFilesByTag(String tagName, Long userId, List<String> userRoles) {
        List<File> files = fileRepository.findByTagName(tagName);

        return files.stream()
                .filter(file -> hasAccessToFile(file, userId, userRoles))
                .map(file -> {
                    FileResponse response = FileResponse.fromEntity(file);
                    response.setDownloadUrl(generateDownloadUrl(file.getFileName()));
                    response.setUploaderName(getUsernameById(file.getUploadedBy()));

                    // Get all tags
                    List<FileTag> tags = fileTagRepository.findByFileId(file.getId());
                    if (!tags.isEmpty()) {
                        response.setTags(tags.stream().map(FileTag::getTagName).collect(Collectors.toList()));
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all available tags
     */
    public List<String> getAllTags() {
        return fileTagRepository.findAllUniqueTagNames();
    }

    /**
     * Create default permissions for a newly uploaded file
     */
    private void createDefaultPermissions(Long fileId, Long userId, boolean isPublic) {
        // Owner has full access
        FilePermission ownerPermission = new FilePermission(fileId, userId, userId);
        ownerPermission.setCanView(true);
        ownerPermission.setCanDownload(true);
        ownerPermission.setCanEdit(true);
        ownerPermission.setCanDelete(true);
        filePermissionRepository.save(ownerPermission);

        if (isPublic) {
            createPublicPermission(fileId, userId);
        }
    }

    /**
     * Create public permission for everyone
     */
    private void createPublicPermission(Long fileId, Long grantedBy) {
        // Public files can be viewed and downloaded by anyone with L1_SUPPORT role or higher
        String[] roles = {"L1_SUPPORT", "L2_SUPPORT", "MANAGER", "ADMIN"};

        for (String role : roles) {
            FilePermission rolePermission = new FilePermission(fileId, role, grantedBy);
            rolePermission.setCanView(true);
            rolePermission.setCanDownload(true);
            filePermissionRepository.save(rolePermission);
        }
    }

    /**
     * Remove public permissions
     */
    private void removePublicPermissions(Long fileId) {
        String[] roles = {"L1_SUPPORT", "L2_SUPPORT", "MANAGER", "ADMIN"};

        for (String role : roles) {
            filePermissionRepository.findByFileIdAndRoleAndIsActive(fileId, role, true)
                    .ifPresent(permission -> {
                        permission.setActive(false);
                        filePermissionRepository.save(permission);
                    });
        }
    }

    /**
     * Check if a user has access to a file
     */
    private boolean hasAccessToFile(File file, Long userId, List<String> userRoles) {
        // File owner has access
        if (file.getUploadedBy().equals(userId)) {
            return true;
        }

        // Public files are accessible to all
        if (file.isPublic()) {
            return true;
        }

        // Check permissions
        return filePermissionRepository.hasViewPermission(file.getId(), userId, userRoles);
    }

    /**
     * Clean up expired permissions
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    public void cleanupExpiredPermissions() {
        int count = filePermissionRepository.deleteExpiredPermissions(LocalDateTime.now());
        log.info("Cleaned up {} expired file permissions", count);
    }

    /**
     * Generate download URL for a file
     */
    private String generateDownloadUrl(String fileName) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/download/")
                .path(fileName)
                .toUriString();
    }

    /**
     * Publish file uploaded event
     */
    private void publishFileUploadedEvent(File file, Long userId) {
        eventPublisher.publishEvent(
                FileEvent.fileUploaded(
                        file.getId(),
                        file.getFileName(),
                        userId,
                        file.getIncidentId(),
                        file.getFileType()
                )
        );
    }

    /**
     * Get username by user ID (mock implementation)
     */
    private String getUsernameById(Long userId) {
        // In a real implementation, this would call the User Service
        return "User " + userId;
    }

    /**
     * Get incident title by incident ID (mock implementation)
     */
    private String getIncidentTitle(Long incidentId) {
        // In a real implementation, this would call the Incident Service
        return "Incident #" + incidentId;
    }
}