package com.incidentcomm.fileservice.controller;

import com.incidentcomm.fileservice.dto.request.FileTagRequest;
import com.incidentcomm.fileservice.dto.response.FileResponse;
import com.incidentcomm.fileservice.dto.response.UploadResponse;
import com.incidentcomm.fileservice.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * Upload a file
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "incidentId", required = false) Long incidentId,
            @RequestParam(value = "messageId", required = false) Long messageId,
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
            @RequestParam(value = "tags", required = false) String tags,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        // Process tags if provided
        List<String> tagList = null;
        if (tags != null && !tags.trim().isEmpty()) {
            tagList = Arrays.asList(tags.split(","));
        }

        UploadResponse response = fileService.uploadFile(
                file, description, incidentId, messageId, isPublic, userId, tagList);

        return ResponseEntity.ok(response);
    }

    /**
     * Download a file
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName,
            HttpServletRequest request) {

        // Get user ID and roles from request attributes
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("userRoles");

        // If user is not authenticated (public download), use default values
        if (userId == null) {
            userId = 0L;
            userRoles = List.of("ANONYMOUS");
        }

        Resource resource = fileService.downloadFile(fileName, userId, userRoles);

        // Determine content type
        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }

        // Fallback to default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Get a file by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFileById(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("userRoles");

        FileResponse file = fileService.getFileById(id, userId, userRoles);
        return ResponseEntity.ok(file);
    }

    /**
     * Get files for an incident
     */
    @GetMapping("/incident/{incidentId}")
    public ResponseEntity<List<FileResponse>> getFilesByIncidentId(
            @PathVariable Long incidentId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("userRoles");

        List<FileResponse> files = fileService.getFilesByIncidentId(incidentId, userId, userRoles);
        return ResponseEntity.ok(files);
    }

    /**
     * Get user's files
     */
    @GetMapping("/user")
    public ResponseEntity<Page<FileResponse>> getUserFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("userRoles");

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<FileResponse> files = fileService.getUserFiles(userId, userRoles, pageable);
        return ResponseEntity.ok(files);
    }

    /**
     * Search files
     */
    @GetMapping("/search")
    public ResponseEntity<Page<FileResponse>> searchFiles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("userRoles");

        Pageable pageable = PageRequest.of(page, size);

        Page<FileResponse> files = fileService.searchFiles(keyword, userId, userRoles, pageable);
        return ResponseEntity.ok(files);
    }

    /**
     * Update file metadata
     */
    @PutMapping("/{id}")
    public ResponseEntity<FileResponse> updateFile(
            @PathVariable Long id,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "false") boolean isPublic,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("userRoles");

        FileResponse file = fileService.updateFile(id, description, isPublic, userId, userRoles);
        return ResponseEntity.ok(file);
    }

    /**
     * Delete a file
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("userRoles");

        fileService.deleteFile(id, userId, userRoles);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add tags to a file
     */
    @PostMapping("/tags")
    public ResponseEntity<FileResponse> addTags(
            @RequestBody FileTagRequest request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) httpRequest.getAttribute("userRoles");

        FileResponse file = fileService.addTags(request, userId, userRoles);
        return ResponseEntity.ok(file);
    }

    /**
     * Remove a tag from a file
     */
    @DeleteMapping("/{id}/tags/{tagName}")
    public ResponseEntity<FileResponse> removeTag(
            @PathVariable Long id,
            @PathVariable String tagName,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("userRoles");

        FileResponse file = fileService.removeTag(id, tagName, userId, userRoles);
        return ResponseEntity.ok(file);
    }

    /**
     * Find files by tag
     */
    @GetMapping("/tags/{tagName}")
    public ResponseEntity<List<FileResponse>> findFilesByTag(
            @PathVariable String tagName,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("userRoles");

        List<FileResponse> files = fileService.findFilesByTag(tagName, userId, userRoles);
        return ResponseEntity.ok(files);
    }

    /**
     * Get all available tags
     */
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        List<String> tags = fileService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * Share a file with a user
     */
    @PostMapping("/{id}/share/user/{userId}")
    public ResponseEntity<FileResponse> shareFileWithUser(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "true") boolean canView,
            @RequestParam(defaultValue = "true") boolean canDownload,
            @RequestParam(defaultValue = "false") boolean canEdit,
            @RequestParam(defaultValue = "false") boolean canDelete,
            @RequestParam(required = false) String expiresAt,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("userRoles");

        // Parse expiration date if provided
        LocalDateTime expirationDate = null;
        if (expiresAt != null && !expiresAt.isEmpty()) {
            expirationDate = LocalDateTime.parse(expiresAt);
        }

        FileResponse file = fileService.shareFileWithUser(
                id, userId, canView, canDownload, canEdit, canDelete, expirationDate, currentUserId, userRoles);

        return ResponseEntity.ok(file);
    }

    /**
     * Share a file with a role
     */
    @PostMapping("/{id}/share/role/{role}")
    public ResponseEntity<FileResponse> shareFileWithRole(
            @PathVariable Long id,
            @PathVariable String role,
            @RequestParam(defaultValue = "true") boolean canView,
            @RequestParam(defaultValue = "true") boolean canDownload,
            @RequestParam(defaultValue = "false") boolean canEdit,
            @RequestParam(defaultValue = "false") boolean canDelete,
            @RequestParam(required = false) String expiresAt,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("userRoles");

        // Parse expiration date if provided
        LocalDateTime expirationDate = null;
        if (expiresAt != null && !expiresAt.isEmpty()) {
            expirationDate = LocalDateTime.parse(expiresAt);
        }

        FileResponse file = fileService.shareFileWithRole(
                id, role, canView, canDownload, canEdit, canDelete, expirationDate, currentUserId, userRoles);

        return ResponseEntity.ok(file);
    }

    /**
     * Remove a file permission
     */
    @DeleteMapping("/{id}/permissions/{permissionId}")
    public ResponseEntity<FileResponse> removeFilePermission(
            @PathVariable Long id,
            @PathVariable Long permissionId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("userRoles");

        FileResponse file = fileService.removeFilePermission(id, permissionId, userId, userRoles);
        return ResponseEntity.ok(file);
    }
}