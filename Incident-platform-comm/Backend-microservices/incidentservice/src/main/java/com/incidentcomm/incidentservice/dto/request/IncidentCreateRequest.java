package com.incidentcomm.incidentservice.dto.request;

import com.incidentcomm.incidentservice.model.Incident;
import com.incidentcomm.incidentservice.model.IncidentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class IncidentCreateRequest {
    @NotBlank
    @Size(min = 5, max = 100)
    private String title;

    @Size(max = 1000)
    private String description;

    private Long assignedTo;

    private IncidentStatus status;

    private Incident.Priority priority;

    private Set<String> tags = new HashSet<>();
}