package com.ofentse.pulse.notification.email;

import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.exception.NotificationNotFoundException;
import com.ofentse.pulse.notification.repository.NotificationRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

        SimpleMailMessage email = new SimpleMailMessage();

        email.setFrom(mailUsername);
        email.setTo(message.getTo());
        email.setSubject(message.getSubject());
        email.setText(message.getContent());

        Notification notification = notificationRepo.findById(message.getNotificationId())
                .orElseThrow(() -> {
                        log.warn("Notification {} not found; message will be retried", message.getNotificationId());

                            return new NotificationNotFoundException(
                                    "notification", "Notification with ID: " + message.getNotificationId() + " not found.");
                        }
                );

        if (notification.getStatus() == NotificationStatus.SENT) {

            log.info("Notification {} already SENT; skipping duplicate email", notification.getId());
            return;
        }

        mailSender.send(email);

        log.info("Email successfully sent for notification {}", notification.getId());

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        notificationRepo.save(notification);

    }

}
