package com.incidentcomm.incidentservice.dto.request;

import com.incidentcomm.incidentservice.model.Incident;
import com.incidentcomm.incidentservice.model.IncidentStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class IncidentUpdateRequest {
    @Size(min = 5, max = 100)
    private String title;

    @Size(max = 1000)
    private String description;

    private Long assignedTo;

    private IncidentStatus status;

    private Incident.Priority priority;

    private Set<String> tags;

    private String comments;
}