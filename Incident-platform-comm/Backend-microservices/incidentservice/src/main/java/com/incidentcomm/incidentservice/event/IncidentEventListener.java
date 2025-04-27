package com.incidentcomm.incidentservice.event;

import com.incidentcomm.incidentservice.model.IncidentHistory;
import com.incidentcomm.incidentservice.repository.IncidentHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IncidentEventListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private IncidentHistoryRepository incidentHistoryRepository;

    // Listen for incident events and broadcast them over WebSocket
    @Async
    @EventListener
    public void handleIncidentEvent(IncidentEvent event) {
        log.info("Processing incident event: {}", event);

        // Create history record if appropriate
        if (event.getEventType() == IncidentEvent.EventType.STATUS_CHANGED ||
                event.getEventType() == IncidentEvent.EventType.ASSIGNED) {

            IncidentHistory history = new IncidentHistory();
            history.setIncidentId(event.getIncidentId());
            history.setModifiedBy(event.getUserId());
            history.setChangeType(event.getEventType().toString());
            history.setComments(event.getMessage());

            if (event.getEventType() == IncidentEvent.EventType.STATUS_CHANGED) {
                history.setOldStatus(event.getOldStatus());
                history.setNewStatus(event.getNewStatus());
            } else if (event.getEventType() == IncidentEvent.EventType.ASSIGNED) {
                history.setOldAssignedTo(event.getOldAssignee());
                history.setNewAssignedTo(event.getNewAssignee());
            }

            incidentHistoryRepository.save(history);
        }

        // Send WebSocket notification to everyone subscribed to this incident
        messagingTemplate.convertAndSend("/topic/incident/" + event.getIncidentId(), event);

        // Also send to a global topic for dashboard updates
        messagingTemplate.convertAndSend("/topic/incidents", event);
    }
}