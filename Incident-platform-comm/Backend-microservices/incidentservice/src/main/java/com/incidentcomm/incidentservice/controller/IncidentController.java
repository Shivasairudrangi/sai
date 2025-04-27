package com.incidentcomm.incidentservice.controller;

import com.incidentcomm.incidentservice.dto.request.IncidentCreateRequest;
import com.incidentcomm.incidentservice.dto.request.IncidentUpdateRequest;
import com.incidentcomm.incidentservice.dto.response.IncidentResponse;
import com.incidentcomm.incidentservice.dto.response.MessageResponse;
import com.incidentcomm.incidentservice.model.Incident;
import com.incidentcomm.incidentservice.model.IncidentHistory;
import com.incidentcomm.incidentservice.model.IncidentStatus;
import com.incidentcomm.incidentservice.service.IncidentHistoryService;
import com.incidentcomm.incidentservice.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin(origins = "*", maxAge = 3600)
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private IncidentHistoryService historyService;

    // Create a new incident
    @PostMapping
    @PreAuthorize("hasAuthority('L1_SUPPORT') or hasAuthority('L2_SUPPORT') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> createIncident(
            @Valid @RequestBody IncidentCreateRequest request,
            @RequestHeader(value = "X-User-ID", required = false) Long headerUserId,
            Authentication authentication) {
        
        Long userId = headerUserId;
        // If X-User-ID header is missing, try to extract from Authentication
        if (userId == null && authentication != null) {
            // Extract username and look up ID
            String username = authentication.getName();
            userId = getUserIdByUsername(username);
        }
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("User ID required but not provided"));
        }

        IncidentResponse incident = incidentService.createIncident(request, userId);
        return new ResponseEntity<>(incident, HttpStatus.CREATED);
    }

    // Get incident by ID
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncidentById(@PathVariable Long id) {
        IncidentResponse incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(incident);
    }

    // Update an incident
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('L1_SUPPORT') or hasAuthority('L2_SUPPORT') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody IncidentUpdateRequest request,
            @RequestHeader(value = "X-User-ID", required = false) Long headerUserId,
            Authentication authentication) {

        Long userId = headerUserId;
        if (userId == null && authentication != null) {
            userId = getUserIdByUsername(authentication.getName());
        }
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("User ID required but not provided"));
        }

        IncidentResponse updatedIncident = incidentService.updateIncident(id, request, userId);
        return ResponseEntity.ok(updatedIncident);
    }

    // Delete an incident
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<MessageResponse> deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.ok(new MessageResponse("Incident deleted successfully"));
    }

    // Get all incidents (paginated)
    @GetMapping
    public ResponseEntity<Page<IncidentResponse>> getAllIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<IncidentResponse> incidents = incidentService.getAllIncidents(pageable);

        return ResponseEntity.ok(incidents);
    }

    // Get incidents assigned to current user
    @GetMapping("/assigned")
    public ResponseEntity<List<IncidentResponse>> getAssignedIncidents(
            @RequestHeader("X-User-ID") Long userId) {

        List<IncidentResponse> incidents = incidentService.getIncidentsByAssignedUser(userId);
        return ResponseEntity.ok(incidents);
    }

    // Get incidents created by current user
    @GetMapping("/created")
    public ResponseEntity<List<IncidentResponse>> getCreatedIncidents(
            @RequestHeader("X-User-ID") Long userId) {

        List<IncidentResponse> incidents = incidentService.getIncidentsByCreator(userId);
        return ResponseEntity.ok(incidents);
    }

    // Get incidents by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByStatus(
            @PathVariable IncidentStatus status) {

        List<IncidentResponse> incidents = incidentService.getIncidentsByStatus(status);
        return ResponseEntity.ok(incidents);
    }

    // Get incidents by priority
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByPriority(
            @PathVariable Incident.Priority priority) {

        List<IncidentResponse> incidents = incidentService.getIncidentsByPriority(priority);
        return ResponseEntity.ok(incidents);
    }

    // Search incidents by keyword
    @GetMapping("/search")
    public ResponseEntity<List<IncidentResponse>> searchIncidents(
            @RequestParam String keyword) {

        List<IncidentResponse> incidents = incidentService.searchIncidents(keyword);
        return ResponseEntity.ok(incidents);
    }

    // Get incidents with a specific tag
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByTag(
            @PathVariable String tag) {

        List<IncidentResponse> incidents = incidentService.getIncidentsByTag(tag);
        return ResponseEntity.ok(incidents);
    }

    // Get high priority unresolved incidents
    @GetMapping("/high-priority")
    @PreAuthorize("hasAuthority('L2_SUPPORT') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<List<IncidentResponse>> getHighPriorityIncidents() {
        List<IncidentResponse> incidents = incidentService.getHighPriorityUnresolved();
        return ResponseEntity.ok(incidents);
    }

    // Get incident history
    @GetMapping("/{id}/history")
    public ResponseEntity<List<IncidentHistory>> getIncidentHistory(
            @PathVariable Long id) {

        List<IncidentHistory> history = historyService.getHistoryForIncident(id);
        return ResponseEntity.ok(history);
    }

    // Get incident statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Map<String, Long>>> getIncidentStats() {
        Map<String, Map<String, Long>> stats = new HashMap<>();
        
        // Get count by status
        Map<String, Long> countByStatus = new HashMap<>();
        for (IncidentStatus status : IncidentStatus.values()) {
            countByStatus.put(status.name(), incidentService.countByStatus(status));
        }
        stats.put("countByStatus", countByStatus);
        
        // Get count by priority
        Map<String, Long> countByPriority = new HashMap<>();
        for (Incident.Priority priority : Incident.Priority.values()) {
            countByPriority.put(priority.name(), incidentService.countByPriority(priority));
        }
        stats.put("countByPriority", countByPriority);
        
        return ResponseEntity.ok(stats);
    }

    // Get paginated incident history
    @GetMapping("/{id}/history/paged")
    public ResponseEntity<Page<IncidentHistory>> getIncidentHistoryPaginated(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "modifiedAt"));
        Page<IncidentHistory> history = historyService.getHistoryForIncidentPaginated(id, pageable);

        return ResponseEntity.ok(history);
    }

    // Add this helper method
    private Long getUserIdByUsername(String username) {
        // Similar to the method in JwtUtils
        Map<String, Long> userIdMap = new HashMap<>();
        userIdMap.put("admin", 8L);
        
        return userIdMap.getOrDefault(username, 1L);
    }
}