package com.incidentcomm.incidentservice.service;

import com.incidentcomm.incidentservice.model.IncidentHistory;
import com.incidentcomm.incidentservice.repository.IncidentHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncidentHistoryService {

    @Autowired
    private IncidentHistoryRepository incidentHistoryRepository;

    // Get all history for a specific incident
    public List<IncidentHistory> getHistoryForIncident(Long incidentId) {
        return incidentHistoryRepository.findByIncidentIdOrderByModifiedAtDesc(incidentId);
    }

    // Get paginated history for a specific incident
    public Page<IncidentHistory> getHistoryForIncidentPaginated(Long incidentId, Pageable pageable) {
        return incidentHistoryRepository.findByIncidentIdOrderByModifiedAtDesc(incidentId, pageable);
    }

    // Get history entries by who modified them
    public List<IncidentHistory> getHistoryByUser(Long userId) {
        return incidentHistoryRepository.findByModifiedBy(userId);
    }

    // Get history for a date range
    public List<IncidentHistory> getHistoryForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return incidentHistoryRepository.findByModifiedAtBetween(startDate, endDate);
    }

    // Get history for specific change type
    public List<IncidentHistory> getHistoryByChangeType(Long incidentId, String changeType) {
        return incidentHistoryRepository.findByIncidentIdAndChangeType(incidentId, changeType);
    }

    // Add manual history entry
    public IncidentHistory addHistoryEntry(IncidentHistory historyEntry) {
        return incidentHistoryRepository.save(historyEntry);
    }
}