package com.incidentcomm.notificationservice.model;

public enum NotificationType {
    // Incident related notifications
    INCIDENT_CREATED,
    INCIDENT_UPDATED,
    INCIDENT_STATUS_CHANGED,
    INCIDENT_ASSIGNED,
    INCIDENT_RESOLVED,

    // Chat related notifications
    CHAT_MESSAGE,
    CHAT_MENTION,
    CHAT_REACTION,

    // File related notifications
    FILE_UPLOADED,
    FILE_SHARED,

    // User related notifications
    USER_JOINED,

    // System notifications
    SYSTEM_ALERT,
    SYSTEM_MAINTENANCE
}