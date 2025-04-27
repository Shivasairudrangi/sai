package com.incidentcomm.incidentservice.model;

public enum IncidentStatus {
    OPEN,           // Newly created incident
    IN_PROGRESS,    // Being worked on
    PENDING,        // Awaiting input or action
    RESOLVED,       // Fixed but not closed
    CLOSED          // Completely done
}