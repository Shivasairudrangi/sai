package com.incidentcomm.incidentservice.dto.response;

import com.incidentcomm.incidentservice.model.Incident;
import com.incidentcomm.incidentservice.model.IncidentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class IncidentResponse {
    private Long id;
    private String title;
    private String description;
    private Long createdBy;
    private String creatorName;  // Will be populated from User Service
    private Long assignedTo;
    private String assigneeName; // Will be populated from User Service
    private IncidentStatus status;
    private Incident.Priority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private Set<String> tags;

    // Constructor to convert from Entity to DTO
    public static IncidentResponse fromEntity(Incident incident) {
        IncidentResponse response = new IncidentResponse();
        response.setId(incident.getId());
        response.setTitle(incident.getTitle());
        response.setDescription(incident.getDescription());
        response.setCreatedBy(incident.getCreatedBy());
        response.setAssignedTo(incident.getAssignedTo());
        response.setStatus(incident.getStatus());
        response.setPriority(incident.getPriority());
        response.setCreatedAt(incident.getCreatedAt());
        response.setUpdatedAt(incident.getUpdatedAt());
        response.setResolvedAt(incident.getResolvedAt());
        response.setTags(incident.getTags());

        return response;
    }
}