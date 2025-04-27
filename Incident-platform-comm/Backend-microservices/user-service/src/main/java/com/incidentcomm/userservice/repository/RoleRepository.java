


package com.incidentcomm.userservice.repository;

import com.incidentcomm.userservice.model.Role;
import com.incidentcomm.userservice.model.Role.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(Role.ERole name);
}