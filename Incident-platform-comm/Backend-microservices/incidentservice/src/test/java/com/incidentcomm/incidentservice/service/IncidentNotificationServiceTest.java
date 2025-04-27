package com.incidentcomm.incidentservice.service;

import com.incidentcomm.incidentservice.dto.response.MessageResponse;
import com.incidentcomm.incidentservice.event.IncidentEvent;
import com.incidentcomm.incidentservice.model.Incident;
import com.incidentcomm.incidentservice.model.IncidentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
class IncidentNotificationServiceTest {

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private IncidentNotificationService notificationService;

    @Test
    void notifyUser_ShouldSendMessageToUserQueue() {
        Long userId = 1L;
        String message = "Test message";

        notificationService.notifyUser(userId, message);

        verify(messagingTemplate).convertAndSendToUser(
            eq(userId.toString()),
            eq("/queue/notifications"),
            eq(new MessageResponse(message))
        );
    }

    @Test
    void notifyRole_ShouldSendMessageToRoleTopic() {
        String role = "ADMIN";
        String message = "Test message";

        notificationService.notifyRole(role, message);

        verify(messagingTemplate).convertAndSend(
            eq("/topic/role/" + role),
            eq(new MessageResponse(message))
        );
    }

    @Test
    void broadcastIncidentEvent_ShouldSendToIncidentAndDashboardTopics() {
        IncidentEvent event = new IncidentEvent(1L, IncidentEvent.EventType.STATUS_CHANGED, 1L);
        event.setOldStatus(IncidentStatus.OPEN);
        event.setNewStatus(IncidentStatus.IN_PROGRESS);

        notificationService.broadcastIncidentEvent(event);

        verify(messagingTemplate).convertAndSend(
            eq("/topic/incident/" + event.getIncidentId()),
            eq(event)
        );
        verify(messagingTemplate).convertAndSend(
            eq("/topic/incidents"),
            eq(event)
        );
    }

    @Test
    void sendIncidentUpdate_ShouldSendCustomPayloadToIncidentTopic() {
        Long incidentId = 1L;
        String message = "Test update";
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");

        notificationService.sendIncidentUpdate(incidentId, message, data);

        verify(messagingTemplate).convertAndSend(
            eq("/topic/incident/" + incidentId),
            any(Map.class)
        );
    }

    @Test
    void notifyHighPriorityIncident_ShouldNotifyManagersAndAdmins() {
        Long incidentId = 1L;
        String title = "Critical Issue";
        String expectedMessage = "High priority incident #" + incidentId + " created: " + title;

        notificationService.notifyHighPriorityIncident(incidentId, title);

        verify(messagingTemplate).convertAndSend(
            eq("/topic/role/MANAGER"),
            eq(new MessageResponse(expectedMessage))
        );
        verify(messagingTemplate).convertAndSend(
            eq("/topic/role/ADMIN"),
            eq(new MessageResponse(expectedMessage))
        );
    }
} 