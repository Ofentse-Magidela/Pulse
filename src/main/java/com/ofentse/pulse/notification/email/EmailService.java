package com.ofentse.pulse.notification.email;

import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.exception.NotificationNotFoundException;
import com.ofentse.pulse.notification.repository.NotificationRepo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final NotificationRepo notificationRepo;

    public EmailService(JavaMailSender mailSender, NotificationRepo notificationRepo) {
        this.mailSender = mailSender;
        this.notificationRepo = notificationRepo;
    }

    @Value("${spring.mail.username}")
    private String mailUsername;

    public void sendEmail(EmailNotificationMessage message) {

        log.info("Processing Email notification {}", message.getNotificationId());

        Notification notification = notificationRepo.findById(message.getNotificationId())
                .orElseThrow(() -> {
                        log.warn("Notification {} not found message will be retried", message.getNotificationId());

                            return new NotificationNotFoundException(
                                    "notification", "Notification with ID: " + message.getNotificationId() + " not found.");
                        }
                );

        if (notification.getStatus() == NotificationStatus.SENT) {

            log.info("Notification {} already SENT skipping duplicate email", notification.getId());
            return;
        }

        try {
            MimeMessage email = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(email, true);

            helper.setFrom(mailUsername);
            helper.setTo(message.getTo());
            helper.setSubject(message.getSubject());
            helper.setText(message.getContent(), true);

            mailSender.send(email);

        } catch (MessagingException e) {

            log.error("Failed to construct email for notification {}", message.getNotificationId(), e);
            throw new RuntimeException("Failed to construct email", e);
        }

        log.info("Email message accepted by provider for notification {}", notification.getId());

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        notificationRepo.save(notification);

    }

}
