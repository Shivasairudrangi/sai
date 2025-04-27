package com.incidentcomm.incidentservice.service;

import com.incidentcomm.incidentservice.dto.request.IncidentCreateRequest;
import com.incidentcomm.incidentservice.dto.request.IncidentUpdateRequest;
import com.incidentcomm.incidentservice.dto.response.IncidentResponse;
import com.incidentcomm.incidentservice.event.IncidentEvent;
import com.incidentcomm.incidentservice.exception.ResourceNotFoundException;
import com.incidentcomm.incidentservice.model.Incident;
import com.incidentcomm.incidentservice.model.IncidentStatus;
import com.incidentcomm.incidentservice.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class IncidentServiceTest {

    @Autowired
    private IncidentService incidentService;

    @MockBean
    private IncidentRepository incidentRepository;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    private Incident testIncident;
    private IncidentCreateRequest createRequest;
    private IncidentUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testIncident = new Incident();
        testIncident.setId(1L);
        testIncident.setTitle("Test Incident");
        testIncident.setDescription("Test Description");
        testIncident.setStatus(IncidentStatus.OPEN);
        testIncident.setPriority(Incident.Priority.HIGH);
        testIncident.setCreatedBy(1L);
        testIncident.setAssignedTo(2L);
        testIncident.setCreatedAt(LocalDateTime.now());
        testIncident.setTags(new HashSet<>(Arrays.asList("test", "critical")));

        createRequest = new IncidentCreateRequest();
        createRequest.setTitle("New Incident");
        createRequest.setPriority(Incident.Priority.HIGH);
        createRequest.setAssignedTo(2L);
        createRequest.setTags(new HashSet<>(Arrays.asList("new", "urgent")));

        updateRequest = new IncidentUpdateRequest();
        updateRequest.setTitle("Updated Incident");
        updateRequest.setDescription("Updated Description");
        updateRequest.setStatus(IncidentStatus.IN_PROGRESS);
        updateRequest.setPriority(Incident.Priority.MEDIUM);
        updateRequest.setAssignedTo(3L);

        reset(incidentRepository, eventPublisher);
    }

    @Test
    void createIncident_Success() {
        // Given
        IncidentCreateRequest request = new IncidentCreateRequest();
        request.setTitle("Test Incident");
        request.setDescription("Test Description");
        request.setPriority(Incident.Priority.HIGH);
        request.setAssignedTo(2L);

        Incident incident = new Incident();
        incident.setId(1L);
        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());
        incident.setPriority(request.getPriority());
        incident.setAssignedTo(request.getAssignedTo());
        incident.setStatus(IncidentStatus.OPEN);

        when(incidentRepository.save(any(Incident.class))).thenReturn(incident);

        // When
        IncidentResponse response = incidentService.createIncident(request, 1L);

        // Then
        assertNotNull(response);
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.getDescription(), response.getDescription());
        assertEquals(request.getPriority(), response.getPriority());
        assertEquals(request.getAssignedTo(), response.getAssignedTo());

        // Verify event publishing
        verify(eventPublisher).publishEvent(any(IncidentEvent.class));
    }

    @Test
    void getIncidentById_Success() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));

        IncidentResponse response = incidentService.getIncidentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Incident", response.getTitle());
    }

    @Test
    void getIncidentById_NotFound() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> incidentService.getIncidentById(1L));
    }

    @Test
    void updateIncident_Success() {
        // Given
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // When
        IncidentResponse response = incidentService.updateIncident(1L, updateRequest, 1L);

        // Then
        assertNotNull(response);
        assertEquals(updateRequest.getTitle(), response.getTitle());
        assertEquals(updateRequest.getDescription(), response.getDescription());
        assertEquals(updateRequest.getStatus(), response.getStatus());
        assertEquals(updateRequest.getPriority(), response.getPriority());
        assertEquals(updateRequest.getAssignedTo(), response.getAssignedTo());

        // Verify events
        verify(eventPublisher, times(2)).publishEvent(any(IncidentEvent.class));
    }

    @Test
    void updateIncident_StatusChange() {
        // Given
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);
        updateRequest.setStatus(IncidentStatus.RESOLVED);

        // When
        incidentService.updateIncident(1L, updateRequest, 1L);

        // Then
        // Verify events
        verify(eventPublisher, times(3)).publishEvent(any(IncidentEvent.class));
    }

    @Test
    void deleteIncident_Success() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));

        incidentService.deleteIncident(1L);

        verify(incidentRepository).delete(testIncident);
    }

    @Test
    void getAllIncidents_Success() {
        Page<Incident> page = new PageImpl<>(List.of(testIncident));
        when(incidentRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<IncidentResponse> response = incidentService.getAllIncidents(PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Test Incident", response.getContent().get(0).getTitle());
    }

    @Test
    void getIncidentsByAssignedUser_Success() {
        when(incidentRepository.findByAssignedTo(2L)).thenReturn(List.of(testIncident));

        List<IncidentResponse> response = incidentService.getIncidentsByAssignedUser(2L);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(1L, response.get(0).getId());
    }

    @Test
    void getIncidentsByStatus_Success() {
        when(incidentRepository.findByStatus(IncidentStatus.OPEN)).thenReturn(List.of(testIncident));

        List<IncidentResponse> response = incidentService.getIncidentsByStatus(IncidentStatus.OPEN);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(IncidentStatus.OPEN, response.get(0).getStatus());
    }

    @Test
    void getHighPriorityUnresolved_Success() {
        when(incidentRepository.findHighPriorityUnresolved()).thenReturn(List.of(testIncident));

        List<IncidentResponse> response = incidentService.getHighPriorityUnresolved();

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(Incident.Priority.HIGH, response.get(0).getPriority());
    }

    @Test
    void searchIncidents_Success() {
        when(incidentRepository.searchByKeyword("test")).thenReturn(List.of(testIncident));

        List<IncidentResponse> response = incidentService.searchIncidents("test");

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals("Test Incident", response.get(0).getTitle());
    }
} 