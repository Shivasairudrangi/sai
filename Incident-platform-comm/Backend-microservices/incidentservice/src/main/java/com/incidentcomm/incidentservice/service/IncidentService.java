package com.incidentcomm.incidentservice.service;

import com.incidentcomm.incidentservice.dto.request.IncidentCreateRequest;
import com.incidentcomm.incidentservice.dto.request.IncidentUpdateRequest;
import com.incidentcomm.incidentservice.dto.response.IncidentResponse;
import com.incidentcomm.incidentservice.event.IncidentEvent;
import com.incidentcomm.incidentservice.exception.ResourceNotFoundException;
import com.incidentcomm.incidentservice.model.Incident;
import com.incidentcomm.incidentservice.model.IncidentStatus;
import com.incidentcomm.incidentservice.repository.IncidentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // Create a new incident
    @Transactional
    public IncidentResponse createIncident(IncidentCreateRequest request, Long userId) {
        Incident incident = new Incident();
        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());
        incident.setCreatedBy(userId);
        incident.setAssignedTo(request.getAssignedTo());
        incident.setStatus(request.getStatus() != null ? request.getStatus() : IncidentStatus.OPEN);
        incident.setPriority(request.getPriority() != null ? request.getPriority() : Incident.Priority.MEDIUM);
        incident.setTags(request.getTags());

        Incident savedIncident = incidentRepository.save(incident);

        // Publish event for WebSocket notifications
        eventPublisher.publishEvent(IncidentEvent.created(savedIncident, userId));

        return IncidentResponse.fromEntity(savedIncident);
    }

    // Get incident by ID
    public IncidentResponse getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        return IncidentResponse.fromEntity(incident);
    }

    // Update an incident
    @Transactional
    public IncidentResponse updateIncident(Long id, IncidentUpdateRequest request, Long userId) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        // Store old values for event tracking
        IncidentStatus oldStatus = incident.getStatus();
        Long oldAssignee = incident.getAssignedTo();

        // Update basic fields if provided
        if (request.getTitle() != null) {
            incident.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            incident.setDescription(request.getDescription());
        }

        // Handle status change
        if (request.getStatus() != null && !request.getStatus().equals(oldStatus)) {
            incident.setStatus(request.getStatus());

            // Publish status change event
            eventPublisher.publishEvent(
                    IncidentEvent.statusChanged(incident, userId, oldStatus));

            // If status changed to RESOLVED, set resolved time
            if (request.getStatus() == IncidentStatus.RESOLVED) {
                incident.setResolvedAt(LocalDateTime.now());
                eventPublisher.publishEvent(IncidentEvent.resolved(incident, userId));
            }
        }

        // Handle assignment change
        if (request.getAssignedTo() != null && !request.getAssignedTo().equals(oldAssignee)) {
            incident.setAssignedTo(request.getAssignedTo());

            // Publish assignment event
            eventPublisher.publishEvent(
                    IncidentEvent.assigned(incident, userId, oldAssignee));
        }

        // Update priority if provided
        if (request.getPriority() != null) {
            incident.setPriority(request.getPriority());
        }

        // Update tags if provided
        if (request.getTags() != null) {
            incident.setTags(request.getTags());
        }

        Incident updatedIncident = incidentRepository.save(incident);

        // Publish general update event
        IncidentEvent event = new IncidentEvent(updatedIncident.getId(), IncidentEvent.EventType.UPDATED, userId);
        event.setMessage("Incident #" + updatedIncident.getId() + " updated");
        eventPublisher.publishEvent(event);

        return IncidentResponse.fromEntity(updatedIncident);
    }

    // Delete an incident
    @Transactional
    public void deleteIncident(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        incidentRepository.delete(incident);
    }

    // Get all incidents (paginated)
    public Page<IncidentResponse> getAllIncidents(Pageable pageable) {
        Page<Incident> incidents = incidentRepository.findAll(pageable);
        return incidents.map(IncidentResponse::fromEntity);
    }

    // Get incidents assigned to a specific user
    public List<IncidentResponse> getIncidentsByAssignedUser(Long userId) {
        return incidentRepository.findByAssignedTo(userId).stream()
                .map(IncidentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Get incidents created by a specific user
    public List<IncidentResponse> getIncidentsByCreator(Long userId) {
        return incidentRepository.findByCreatedBy(userId).stream()
                .map(IncidentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Get incidents by status
    public List<IncidentResponse> getIncidentsByStatus(IncidentStatus status) {
        return incidentRepository.findByStatus(status).stream()
                .map(IncidentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Get incidents by priority
    public List<IncidentResponse> getIncidentsByPriority(Incident.Priority priority) {
        return incidentRepository.findByPriority(priority).stream()
                .map(IncidentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Search incidents by keyword
    public List<IncidentResponse> searchIncidents(String keyword) {
        return incidentRepository.searchByKeyword(keyword).stream()
                .map(IncidentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Get incidents with a specific tag
    public List<IncidentResponse> getIncidentsByTag(String tag) {
        return incidentRepository.findByTagsContaining(tag).stream()
                .map(IncidentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Get high priority unresolved incidents
    public List<IncidentResponse> getHighPriorityUnresolved() {
        return incidentRepository.findHighPriorityUnresolved().stream()
                .map(IncidentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Count incidents by status
    public Long countByStatus(IncidentStatus status) {
        return incidentRepository.countByStatus(status);
    }

    // Count incidents by priority
    public Long countByPriority(Incident.Priority priority) {
        return incidentRepository.countByPriority(priority);
    }
}