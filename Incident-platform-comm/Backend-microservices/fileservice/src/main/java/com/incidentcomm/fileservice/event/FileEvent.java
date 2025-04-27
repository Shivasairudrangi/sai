package com.incidentcomm.fileservice.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileEvent {

    public enum EventType {
        UPLOADED,
        UPDATED,
        DOWNLOADED,
        DELETED,
        SHARED,
        PERMISSION_CHANGED,
        TAG_ADDED,
        TAG_REMOVED
    }

    private EventType type;
    private Long fileId;
    private String fileName;
    private Long userId;
    private String username;
    private Long incidentId;
    private Long messageId;
    private String fileType;
    private LocalDateTime timestamp;
    private Object additionalData;

    // Constructors
    public FileEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public FileEvent(EventType type, Long fileId, String fileName, Long userId) {
        this.type = type;
        this.fileId = fileId;
        this.fileName = fileName;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    // Factory methods for different events

    public static FileEvent fileUploaded(Long fileId, String fileName, Long userId, Long incidentId, String fileType) {
        FileEvent event = new FileEvent(EventType.UPLOADED, fileId, fileName, userId);
        event.setIncidentId(incidentId);
        event.setFileType(fileType);
        return event;
    }

    public static FileEvent fileUpdated(Long fileId, String fileName, Long userId) {
        return new FileEvent(EventType.UPDATED, fileId, fileName, userId);
    }

    public static FileEvent fileDownloaded(Long fileId, String fileName, Long userId) {
        return new FileEvent(EventType.DOWNLOADED, fileId, fileName, userId);
    }

    public static FileEvent fileDeleted(Long fileId, String fileName, Long userId) {
        return new FileEvent(EventType.DELETED, fileId, fileName, userId);
    }

    public static FileEvent fileShared(Long fileId, String fileName, Long userId, Long targetUserId) {
        FileEvent event = new FileEvent(EventType.SHARED, fileId, fileName, userId);
        event.setAdditionalData(targetUserId);
        return event;
    }

    public static FileEvent permissionChanged(Long fileId, String fileName, Long userId, Object permissions) {
        FileEvent event = new FileEvent(EventType.PERMISSION_CHANGED, fileId, fileName, userId);
        event.setAdditionalData(permissions);
        return event;
    }

    public static FileEvent tagAdded(Long fileId, String fileName, Long userId, String tag) {
        FileEvent event = new FileEvent(EventType.TAG_ADDED, fileId, fileName, userId);
        event.setAdditionalData(tag);
        return event;
    }

    public static FileEvent tagRemoved(Long fileId, String fileName, Long userId, String tag) {
        FileEvent event = new FileEvent(EventType.TAG_REMOVED, fileId, fileName, userId);
        event.setAdditionalData(tag);
        return event;
    }
}