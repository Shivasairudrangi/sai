package com.incidentcomm.incidentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidentcomm.incidentservice.dto.request.IncidentCreateRequest;
import com.incidentcomm.incidentservice.dto.request.IncidentUpdateRequest;
import com.incidentcomm.incidentservice.dto.response.IncidentResponse;
import com.incidentcomm.incidentservice.dto.response.MessageResponse;
import com.incidentcomm.incidentservice.model.Incident;
import com.incidentcomm.incidentservice.model.IncidentHistory;
import com.incidentcomm.incidentservice.model.IncidentStatus;
import com.incidentcomm.incidentservice.service.IncidentHistoryService;
import com.incidentcomm.incidentservice.service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IncidentService incidentService;

    @MockBean
    private IncidentHistoryService historyService;

    private IncidentResponse sampleIncidentResponse;
    private IncidentCreateRequest sampleCreateRequest;
    private IncidentUpdateRequest sampleUpdateRequest;

    @BeforeEach
    void setUp() {
        // Setup sample incident response
        sampleIncidentResponse = new IncidentResponse();
        sampleIncidentResponse.setId(1L);
        sampleIncidentResponse.setTitle("Test Incident");
        sampleIncidentResponse.setDescription("Test Description");
        sampleIncidentResponse.setStatus(IncidentStatus.OPEN);
        sampleIncidentResponse.setPriority(Incident.Priority.HIGH);
        sampleIncidentResponse.setCreatedAt(LocalDateTime.now());
        sampleIncidentResponse.setCreatedBy(1L);
        sampleIncidentResponse.setAssignedTo(2L);
        sampleIncidentResponse.setTags(new HashSet<>(Arrays.asList("test", "critical")));

        // Setup sample create request
        sampleCreateRequest = new IncidentCreateRequest();
        sampleCreateRequest.setTitle("New Incident");
        sampleCreateRequest.setDescription("New Description");
        sampleCreateRequest.setPriority(Incident.Priority.HIGH);
        sampleCreateRequest.setAssignedTo(2L);
        sampleCreateRequest.setTags(new HashSet<>(Arrays.asList("new", "urgent")));

        // Setup sample update request
        sampleUpdateRequest = new IncidentUpdateRequest();
        sampleUpdateRequest.setTitle("Updated Incident");
        sampleUpdateRequest.setDescription("Updated Description");
        sampleUpdateRequest.setStatus(IncidentStatus.IN_PROGRESS);
        sampleUpdateRequest.setPriority(Incident.Priority.MEDIUM);
        sampleUpdateRequest.setAssignedTo(3L);
    }

    @Test
    @WithMockUser(authorities = "L1_SUPPORT")
    void createIncident_Success() throws Exception {
        when(incidentService.createIncident(any(IncidentCreateRequest.class), anyLong()))
                .thenReturn(sampleIncidentResponse);

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-ID", "1")
                        .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Incident"));
    }

    @Test
    @WithMockUser
    void getIncidentById_Success() throws Exception {
        when(incidentService.getIncidentById(1L)).thenReturn(sampleIncidentResponse);

        mockMvc.perform(get("/api/incidents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Incident"));
    }

    @Test
    @WithMockUser(authorities = "L2_SUPPORT")
    void updateIncident_Success() throws Exception {
        when(incidentService.updateIncident(eq(1L), any(IncidentUpdateRequest.class), anyLong()))
                .thenReturn(sampleIncidentResponse);

        mockMvc.perform(put("/api/incidents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-ID", "1")
                        .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteIncident_Success() throws Exception {
        mockMvc.perform(delete("/api/incidents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Incident deleted successfully"));
    }

    @Test
    @WithMockUser
    void getAllIncidents_Success() throws Exception {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<IncidentResponse> page = new PageImpl<>(List.of(sampleIncidentResponse), pageRequest, 1);
        
        when(incidentService.getAllIncidents(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/incidents")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Incident"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @WithMockUser
    void getAssignedIncidents_Success() throws Exception {
        when(incidentService.getIncidentsByAssignedUser(anyLong()))
                .thenReturn(List.of(sampleIncidentResponse));

        mockMvc.perform(get("/api/incidents/assigned")
                        .header("X-User-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser
    void getIncidentsByStatus_Success() throws Exception {
        when(incidentService.getIncidentsByStatus(any(IncidentStatus.class)))
                .thenReturn(List.of(sampleIncidentResponse));

        mockMvc.perform(get("/api/incidents/status/OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser
    void getIncidentHistory_Success() throws Exception {
        IncidentHistory history = new IncidentHistory();
        history.setId(1L);
        history.setIncidentId(1L);
        history.setModifiedBy(1L);
        history.setModifiedAt(LocalDateTime.now());
        history.setComments("Status changed from OPEN to IN_PROGRESS");

        when(historyService.getHistoryForIncident(1L))
                .thenReturn(List.of(history));

        mockMvc.perform(get("/api/incidents/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].incidentId").value(1));
    }

    @Test
    @WithMockUser(authorities = "L2_SUPPORT")
    void getHighPriorityIncidents_Success() throws Exception {
        when(incidentService.getHighPriorityUnresolved())
                .thenReturn(List.of(sampleIncidentResponse));

        mockMvc.perform(get("/api/incidents/high-priority"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].priority").value("HIGH"));
    }

    @Test
    @WithMockUser
    void searchIncidents_Success() throws Exception {
        when(incidentService.searchIncidents(anyString()))
                .thenReturn(List.of(sampleIncidentResponse));

        mockMvc.perform(get("/api/incidents/search")
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
} 