package com.incidentcomm.incidentservice.repository;

import com.incidentcomm.incidentservice.model.Incident;
import com.incidentcomm.incidentservice.model.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    // Find incidents by assigned user
    List<Incident> findByAssignedTo(Long userId);

    // Find incidents by creator
    List<Incident> findByCreatedBy(Long userId);

    // Find incidents by status
    List<Incident> findByStatus(IncidentStatus status);

    // Find incidents by priority
    List<Incident> findByPriority(Incident.Priority priority);

    // Find incidents with specific tag
    @Query("SELECT i FROM Incident i JOIN i.tags t WHERE t = :tag")
    List<Incident> findByTagsContaining(@Param("tag") String tag);

    // Search incidents by title or description containing keyword
    @Query("SELECT i FROM Incident i WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Incident> searchByKeyword(@Param("keyword") String keyword);

    // Find incidents created between dates
    List<Incident> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find open incidents (paginated)
    Page<Incident> findByStatusNot(IncidentStatus status, Pageable pageable);

    // Find incidents that need attention (high priority and not resolved)
    @Query("SELECT i FROM Incident i WHERE i.priority IN ('HIGH', 'CRITICAL') AND i.status NOT IN ('RESOLVED', 'CLOSED')")
    List<Incident> findHighPriorityUnresolved();

    // Count methods for statistics
    Long countByStatus(IncidentStatus status);
    
    Long countByPriority(Incident.Priority priority);
}