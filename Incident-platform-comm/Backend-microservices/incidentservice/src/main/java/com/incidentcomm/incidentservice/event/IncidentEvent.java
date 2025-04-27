package com.incidentcomm.incidentservice.event;

import com.incidentcomm.incidentservice.model.Incident;
import com.incidentcomm.incidentservice.model.IncidentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IncidentEvent {
    public enum EventType {
        CREATED,
        UPDATED,
        STATUS_CHANGED,
        ASSIGNED,
        COMMENT_ADDED,
        RESOLVED,
        CLOSED
    }

    private Long incidentId;
    private EventType eventType;
    private Long userId;  // Who triggered the event
    private String message;
    private LocalDateTime timestamp;
    private IncidentStatus oldStatus;
    private IncidentStatus newStatus;
    private Long oldAssignee;
    private Long newAssignee;

    // Constructor for different event types
    public IncidentEvent(Long incidentId, EventType eventType, Long userId) {
        this.incidentId = incidentId;
        this.eventType = eventType;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    // Specialized constructors for common events
    public static IncidentEvent created(Incident incident, Long userId) {
        IncidentEvent event = new IncidentEvent(incident.getId(), EventType.CREATED, userId);
        event.setMessage("Incident #" + incident.getId() + " created: " + incident.getTitle());
        return event;
    }

    public static IncidentEvent statusChanged(Incident incident, Long userId, IncidentStatus oldStatus) {
        IncidentEvent event = new IncidentEvent(incident.getId(), EventType.STATUS_CHANGED, userId);
        event.setOldStatus(oldStatus);
        event.setNewStatus(incident.getStatus());
        event.setMessage("Incident #" + incident.getId() + " status changed from " +
                oldStatus + " to " + incident.getStatus());
        return event;
    }

    public static IncidentEvent assigned(Incident incident, Long userId, Long oldAssignee) {
        IncidentEvent event = new IncidentEvent(incident.getId(), EventType.ASSIGNED, userId);
        event.setOldAssignee(oldAssignee);
        event.setNewAssignee(incident.getAssignedTo());
        event.setMessage("Incident #" + incident.getId() + " assigned to user #" + incident.getAssignedTo());
        return event;
    }

    public static IncidentEvent resolved(Incident incident, Long userId) {
        IncidentEvent event = new IncidentEvent(incident.getId(), EventType.RESOLVED, userId);
        event.setMessage("Incident #" + incident.getId() + " has been resolved");
        return event;
    }
}