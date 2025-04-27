package com.incidentcomm.notificationservice.service;

import com.incidentcomm.notificationservice.model.Notification;
import com.incidentcomm.notificationservice.model.NotificationChannel;
import com.incidentcomm.notificationservice.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChannelService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send notification via the specified channel
     */
    public void sendViaChannel(Notification notification, NotificationChannel channel) {
        try {
            switch (channel) {
                case IN_APP:
                    sendInAppNotification(notification);
                    break;
                case EMAIL:
                    sendEmailNotification(notification);
                    break;
                case WEBSOCKET:
                    sendWebSocketNotification(notification);
                    break;
                case PUSH:
                    sendPushNotification(notification);
                    break;
                case SMS:
                    sendSMSNotification(notification);
                    break;
                default:
                    log.warn("Unsupported channel: {}", channel);
                    return;
            }

            // Mark as delivered
            notification.markAsDelivered(channel);
            notificationRepository.save(notification);

            log.info("Notification {} sent to user {} via {}",
                    notification.getId(), notification.getUserId(), channel);
        } catch (Exception e) {
            log.error("Failed to send notification via {}: {}", channel, e.getMessage());
        }
    }

    /**
     * Send in-app notification (stored in database)
     */
    private void sendInAppNotification(Notification notification) {
        // In-app notifications are already stored in the database
        // This method could handle additional in-app specific logic
        log.debug("In-app notification stored: {}", notification.getId());
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(Notification notification) {
        emailService.sendNotificationEmail(notification);
    }

    /**
     * Send WebSocket notification
     */
    private void sendWebSocketNotification(Notification notification) {
        try {
            // Send to the specific user's queue
            messagingTemplate.convertAndSendToUser(
                    notification.getUserId().toString(),
                    "/queue/notifications",
                    notification
            );

            // If it's related to a source, send to the source's topic
            if (notification.getSourceId() != null && notification.getSourceType() != null) {
                String topic = String.format("/topic/%s/%d",
                        notification.getSourceType().toLowerCase(),
                        notification.getSourceId());

                messagingTemplate.convertAndSend(topic, notification);
            }

            log.debug("WebSocket notification sent to user {}", notification.getUserId());
        } catch (Exception e) {
            log.error("Error sending WebSocket notification", e);
        }
    }

    /**
     * Send push notification
     * Note: This would integrate with a push notification service like Firebase Cloud Messaging
     */
    private void sendPushNotification(Notification notification) {
        // Mock implementation for now
        log.info("Push notification (MOCK) would be sent to user {}: {}",
                notification.getUserId(), notification.getTitle());
        // In a real implementation, you would:
        // 1. Get the user's push token from a database
        // 2. Send the notification to the push service
    }

    /**
     * Send SMS notification
     * Note: This would integrate with an SMS service like Twilio
     */
    private void sendSMSNotification(Notification notification) {
        // Mock implementation for now
        log.info("SMS notification (MOCK) would be sent to user {}: {}",
                notification.getUserId(), notification.getMessage());
        // In a real implementation, you would:
        // 1. Get the user's phone number from their preferences
        // 2. Format the message appropriately for SMS
        // 3. Send via an SMS service provider
    }
}