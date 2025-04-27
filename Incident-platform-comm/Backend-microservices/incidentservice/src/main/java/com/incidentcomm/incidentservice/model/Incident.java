package com.incidentcomm.incidentservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ElementCollection
    @CollectionTable(name = "incident_tags",
            joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    // Incident priority levels
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    // Pre-persist hook to set creation time
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = IncidentStatus.OPEN;
        }
    }

    // Pre-update hook to update the updated_at timestamp
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        // When an incident is resolved, set the resolved time
        if (status == IncidentStatus.RESOLVED && resolvedAt == null) {
            resolvedAt = LocalDateTime.now();
        }
    }
}