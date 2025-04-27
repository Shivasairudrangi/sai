package com.incidentcomm.chatservice.model;

public enum MessageType {
    TEXT,       // Regular text message
    FILE,       // File attachment
    IMAGE,      // Image attachment
    SYSTEM,     // System message (user joined, left, etc.)
    TYPING,     // Typing indicator
    REACTION,   // Message reaction
    MENTION     // User mention
}