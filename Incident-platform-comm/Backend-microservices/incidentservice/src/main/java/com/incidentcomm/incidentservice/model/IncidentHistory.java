package com.incidentcomm.incidentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id")
    private Long incidentId;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private IncidentStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private IncidentStatus newStatus;

    @Column(name = "old_assigned_to")
    private Long oldAssignedTo;

    @Column(name = "new_assigned_to")
    private Long newAssignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_priority")
    private Incident.Priority oldPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_priority")
    private Incident.Priority newPriority;

    @Column(name = "comments", length = 1000)
    private String comments;

    @Column(name = "change_type")
    private String changeType;

    // Pre-persist hook to set creation time
    @PrePersist
    protected void onCreate() {
        modifiedAt = LocalDateTime.now();
    }
}