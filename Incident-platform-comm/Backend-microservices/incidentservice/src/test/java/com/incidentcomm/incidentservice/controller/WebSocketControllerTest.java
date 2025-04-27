package com.incidentcomm.incidentservice.controller;

import com.incidentcomm.incidentservice.dto.response.MessageResponse;
import com.incidentcomm.incidentservice.event.IncidentEvent;
import com.incidentcomm.incidentservice.model.IncidentStatus;
import com.incidentcomm.incidentservice.service.IncidentNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class WebSocketControllerTest {

    @Autowired
    private WebSocketController webSocketController;

    @MockBean
    private IncidentNotificationService notificationService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    private Principal mockPrincipal;
    private SimpMessageHeaderAccessor headerAccessor;

    @BeforeEach
    void setUp() {
        // Setup mock principal
        mockPrincipal = () -> "testUser";

        // Setup header accessor
        headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setUser(mockPrincipal);
    }

    @Test
    void subscribeToIncident_Success() {
        // Test subscription
        MessageResponse response = webSocketController.subscribeToIncident(1L, mockPrincipal);

        // Verify response
        assertNotNull(response);
        assertEquals("Successfully subscribed to incident #1", response.getMessage());
    }

    @Test
    void sendIncidentMessage_Success() {
        // Prepare test message
        Map<String, Object> message = new HashMap<>();
        message.put("content", "Test message");

        // Send message
        Map<String, Object> response = webSocketController.sendIncidentMessage(1L, message, headerAccessor);

        // Verify response
        assertNotNull(response);
        assertEquals("testUser", response.get("sender"));
        assertEquals("Test message", response.get("content"));
        assertNotNull(response.get("timestamp"));
    }

    @Test
    void updateIncidentStatus_Success() {
        // Prepare test event
        IncidentEvent event = new IncidentEvent(1L, IncidentEvent.EventType.STATUS_CHANGED, 1L);
        event.setNewStatus(IncidentStatus.IN_PROGRESS);

        // Update status
        IncidentEvent response = webSocketController.updateIncidentStatus(1L, event, headerAccessor);

        // Verify response
        assertNotNull(response);
        assertEquals(1L, response.getIncidentId());
        assertEquals(IncidentEvent.EventType.STATUS_CHANGED, response.getEventType());
        assertEquals(IncidentStatus.IN_PROGRESS, response.getNewStatus());
    }

    @Test
    void heartbeat_Success() {
        // Send heartbeat
        MessageResponse response = webSocketController.heartbeat(mockPrincipal);

        // Verify response
        assertNotNull(response);
        assertEquals("pong", response.getMessage());
    }
} 