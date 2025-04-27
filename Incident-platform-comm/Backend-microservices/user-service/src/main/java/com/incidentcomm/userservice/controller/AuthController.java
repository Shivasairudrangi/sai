package com.incidentcomm.userservice.controller;

import com.incidentcomm.userservice.dto.request.LoginRequest;
import com.incidentcomm.userservice.dto.request.RegisterRequest;
import com.incidentcomm.userservice.dto.response.JwtResponse;
import com.incidentcomm.userservice.dto.response.MessageResponse;
import com.incidentcomm.userservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            MessageResponse response = authService.registerUser(registerRequest);
    
            if (response.getMessage().startsWith("Error:")) {
                return ResponseEntity.badRequest().body(response);
            }
    
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}