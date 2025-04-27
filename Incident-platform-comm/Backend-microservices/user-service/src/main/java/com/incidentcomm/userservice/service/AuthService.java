package com.incidentcomm.userservice.service;

import com.incidentcomm.userservice.dto.request.LoginRequest;
import com.incidentcomm.userservice.dto.request.RegisterRequest;
import com.incidentcomm.userservice.dto.response.JwtResponse;
import com.incidentcomm.userservice.dto.response.MessageResponse;
import com.incidentcomm.userservice.model.Role;
import com.incidentcomm.userservice.model.User;
import com.incidentcomm.userservice.repository.RoleRepository;
import com.incidentcomm.userservice.repository.UserRepository;
import com.incidentcomm.userservice.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        return new JwtResponse(jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles);
    }

    public MessageResponse registerUser(RegisterRequest registerRequest) {
        // Check if username exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return new MessageResponse("Error: Username is already taken!");
        }

        // Check if email exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return new MessageResponse("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                encoder.encode(registerRequest.getPassword()));

        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());

        Set<String> strRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role l1Role = roleRepository.findByName(Role.ERole.L1_SUPPORT)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(l1Role);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(Role.ERole.ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "l1":
                        Role l1Role = roleRepository.findByName(Role.ERole.L1_SUPPORT)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(l1Role);
                        break;
                    case "l2":
                        Role l2Role = roleRepository.findByName(Role.ERole.L2_SUPPORT)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(l2Role);
                        break;
                    case "manager":
                        Role managerRole = roleRepository.findByName(Role.ERole.MANAGER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(managerRole);
                        break;
                    default:
                        Role l1DefaultRole = roleRepository.findByName(Role.ERole.L1_SUPPORT)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(l1DefaultRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return new MessageResponse("User registered successfully!");
    }
}