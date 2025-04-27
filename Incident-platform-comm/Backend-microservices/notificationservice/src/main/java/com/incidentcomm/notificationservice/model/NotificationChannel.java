package com.incidentcomm.notificationservice.model;

public enum NotificationChannel {
    IN_APP,        // Notifications within the application
    EMAIL,         // Email notifications
    WEBSOCKET,     // Real-time websocket notifications
    PUSH,          // Mobile push notifications
    SMS            // Text message notifications
}