package com.incidentcomm.incidentservice.service;

import com.incidentcomm.incidentservice.model.Incident;
import com.incidentcomm.incidentservice.model.IncidentHistory;
import com.incidentcomm.incidentservice.model.IncidentStatus;
import com.incidentcomm.incidentservice.repository.IncidentHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class IncidentHistoryServiceTest {

    @Autowired
    private IncidentHistoryService historyService;

    @MockBean
    private IncidentHistoryRepository historyRepository;

    private IncidentHistory testHistory;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();
        
        testHistory = new IncidentHistory();
        testHistory.setId(1L);
        testHistory.setIncidentId(1L);
        testHistory.setModifiedBy(1L);
        testHistory.setModifiedAt(testTime);
        testHistory.setOldStatus(IncidentStatus.OPEN);
        testHistory.setNewStatus(IncidentStatus.IN_PROGRESS);
        testHistory.setOldAssignedTo(1L);
        testHistory.setNewAssignedTo(2L);
        testHistory.setOldPriority(Incident.Priority.LOW);
        testHistory.setNewPriority(Incident.Priority.HIGH);
        testHistory.setComments("Status and priority updated");
        testHistory.setChangeType("STATUS_CHANGE");
    }

    @Test
    void getHistoryForIncident_Success() {
        when(historyRepository.findByIncidentIdOrderByModifiedAtDesc(1L))
                .thenReturn(List.of(testHistory));

        List<IncidentHistory> history = historyService.getHistoryForIncident(1L);

        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertEquals(1L, history.get(0).getId());
        assertEquals("STATUS_CHANGE", history.get(0).getChangeType());
    }

    @Test
    void getHistoryForIncidentPaginated_Success() {
        Page<IncidentHistory> page = new PageImpl<>(List.of(testHistory));
        when(historyRepository.findByIncidentIdOrderByModifiedAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(page);

        Page<IncidentHistory> history = historyService.getHistoryForIncidentPaginated(1L, PageRequest.of(0, 10));

        assertNotNull(history);
        assertEquals(1, history.getTotalElements());
        assertEquals(1L, history.getContent().get(0).getId());
    }

    @Test
    void getHistoryByUser_Success() {
        when(historyRepository.findByModifiedBy(1L))
                .thenReturn(List.of(testHistory));

        List<IncidentHistory> history = historyService.getHistoryByUser(1L);

        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertEquals(1L, history.get(0).getModifiedBy());
    }

    @Test
    void getHistoryForDateRange_Success() {
        LocalDateTime startDate = testTime.minusDays(1);
        LocalDateTime endDate = testTime.plusDays(1);

        when(historyRepository.findByModifiedAtBetween(startDate, endDate))
                .thenReturn(List.of(testHistory));

        List<IncidentHistory> history = historyService.getHistoryForDateRange(startDate, endDate);

        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertTrue(history.get(0).getModifiedAt().isAfter(startDate));
        assertTrue(history.get(0).getModifiedAt().isBefore(endDate));
    }

    @Test
    void getHistoryByChangeType_Success() {
        when(historyRepository.findByIncidentIdAndChangeType(1L, "STATUS_CHANGE"))
                .thenReturn(List.of(testHistory));

        List<IncidentHistory> history = historyService.getHistoryByChangeType(1L, "STATUS_CHANGE");

        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertEquals("STATUS_CHANGE", history.get(0).getChangeType());
    }

    @Test
    void addHistoryEntry_Success() {
        when(historyRepository.save(any(IncidentHistory.class)))
                .thenReturn(testHistory);

        IncidentHistory savedHistory = historyService.addHistoryEntry(testHistory);

        assertNotNull(savedHistory);
        assertEquals(1L, savedHistory.getId());
        assertEquals("STATUS_CHANGE", savedHistory.getChangeType());
        verify(historyRepository).save(testHistory);
    }
} 