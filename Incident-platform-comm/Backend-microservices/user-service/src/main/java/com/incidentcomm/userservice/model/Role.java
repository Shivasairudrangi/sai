package com.incidentcomm.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ERole name;

    public Role(ERole name) {
        this.name = name;
    }

    public enum ERole {
        L1_SUPPORT,  // Level 1 support
        L2_SUPPORT,  // Level 2 support
        MANAGER,     // Manager role
        ADMIN        // Admin role
    }
}