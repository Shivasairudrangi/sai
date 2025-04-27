package com.incidentcomm.fileservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class FileEventListener {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.service.incident}")
    private String incidentServiceUrl;

    @Value("${app.service.chat}")
    private String chatServiceUrl;

    @Async
    @EventListener
    public void handleFileEvent(FileEvent event) {
        log.info("Handling file event: {} for file ID: {}", event.getType(), event.getFileId());

        switch (event.getType()) {
            case UPLOADED:
                handleFileUploadEvent(event);
                break;
            case DOWNLOADED:
                // Possibly track download metrics
                log.info("File download tracked: {}", event.getFileName());
                break;
            case UPDATED:
                notifyRelatedServices(event);
                break;
            case DELETED:
                notifyRelatedServices(event);
                break;
            case SHARED:
                // Notify the user who received access
                Long targetUserId = (Long) event.getAdditionalData();
                log.info("File shared with user ID: {}", targetUserId);
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
        }
    }

    private void handleFileUploadEvent(FileEvent event) {
        // If the file is related to an incident, notify the incident service
        if (event.getIncidentId() != null) {
            notifyIncidentService(event);
        }

        // If the file is related to a message, notify the chat service
        if (event.getMessageId() != null) {
            notifyChatService(event);
        }

        log.info("File upload processed: {}", event.getFileName());
    }

    private void notifyIncidentService(FileEvent event) {
        try {
            // In a real implementation, this would make an API call to the incident service
            // String url = incidentServiceUrl + "/api/incidents/" + event.getIncidentId() + "/files";
            // restTemplate.postForObject(url, createNotificationPayload(event), Object.class);

            log.info("Notified incident service about file: {} for incident ID: {}",
                    event.getFileName(), event.getIncidentId());
        } catch (Exception e) {
            log.error("Failed to notify incident service", e);
        }
    }

    private void notifyChatService(FileEvent event) {
        try {
            // In a real implementation, this would make an API call to the chat service
            // String url = chatServiceUrl + "/api/messages/" + event.getMessageId() + "/files";
            // restTemplate.postForObject(url, createNotificationPayload(event), Object.class);

            log.info("Notified chat service about file: {} for message ID: {}",
                    event.getFileName(), event.getMessageId());
        } catch (Exception e) {
            log.error("Failed to notify chat service", e);
        }
    }

    private void notifyRelatedServices(FileEvent event) {
        // Notify any service that might be interested in file updates or deletions
        if (event.getIncidentId() != null) {
            notifyIncidentService(event);
        }

        if (event.getMessageId() != null) {
            notifyChatService(event);
        }
    }

    private Object createNotificationPayload(FileEvent event) {
        // Create a simplified payload with essential information
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("fileId", event.getFileId());
        payload.put("fileName", event.getFileName());
        payload.put("eventType", event.getType().toString());
        payload.put("timestamp", event.getTimestamp());
        payload.put("userId", event.getUserId());

        if (event.getFileType() != null) {
            payload.put("fileType", event.getFileType());
        }

        return payload;
    }
}