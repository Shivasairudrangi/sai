package com.incidentcomm.incidentservice.repository;

import com.incidentcomm.incidentservice.model.IncidentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentHistoryRepository extends JpaRepository<IncidentHistory, Long> {
    // Find history for a specific incident
    List<IncidentHistory> findByIncidentIdOrderByModifiedAtDesc(Long incidentId);

    // Find history entries by who modified them
    List<IncidentHistory> findByModifiedBy(Long userId);

    // Find history entries within a time range
    List<IncidentHistory> findByModifiedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find history by incident ID and change type
    List<IncidentHistory> findByIncidentIdAndChangeType(Long incidentId, String changeType);

    // Get paginated history for an incident
    Page<IncidentHistory> findByIncidentIdOrderByModifiedAtDesc(Long incidentId, Pageable pageable);
}