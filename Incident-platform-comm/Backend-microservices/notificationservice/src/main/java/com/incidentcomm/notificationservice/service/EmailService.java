package com.incidentcomm.notificationservice.service;

import com.incidentcomm.notificationservice.model.Notification;
import com.incidentcomm.notificationservice.model.NotificationChannel;
import com.incidentcomm.notificationservice.model.NotificationPreference;
import com.incidentcomm.notificationservice.repository.NotificationPreferenceRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Optional;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send a notification email
     */
    public void sendNotificationEmail(Notification notification) {
        try {
            // Get email address from preferences
            Optional<NotificationPreference> emailPref = preferenceRepository
                    .findByUserIdAndNotificationTypeAndChannel(
                            notification.getUserId(),
                            notification.getType(),
                            NotificationChannel.EMAIL);

            if (emailPref.isEmpty() || emailPref.get().getDeliveryAddress() == null) {
                log.warn("No email address found for user {}", notification.getUserId());
                return;
            }

            String toEmail = emailPref.get().getDeliveryAddress();

            // Create context for email template
            Context context = new Context();
            context.setVariable("title", notification.getTitle());
            context.setVariable("message", notification.getMessage());
            context.setVariable("actionUrl", notification.getActionUrl());
            context.setVariable("actionText", notification.getActionText());
            context.setVariable("notificationType", notification.getType());
            context.setVariable("priority", notification.getPriority());
            context.setVariable("createdAt", notification.getCreatedAt());

            // Process template
            String htmlContent = templateEngine.process("notification-email", context);

            // Send email
            sendHtmlEmail(toEmail, notification.getTitle(), htmlContent);

            log.info("Email notification sent to {} for notification {}", toEmail, notification.getId());
        } catch (Exception e) {
            log.error("Failed to send email for notification {}: {}", notification.getId(), e.getMessage());
        }
    }

    /**
     * Send an HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}