package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.dto.EmailNotificationDTO;
import com.ofentse.pulse.notification.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.enums.NotificationChannel;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.producer.NotificationProducer;
import com.ofentse.pulse.notification.repository.NotificationRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationRepo repo;
    private final NotificationProducer producer;
    public NotificationService(NotificationRepo repo, NotificationProducer producer) {
        this.repo = repo;
        this.producer = producer;
    }

    public void sendEmailNotification(EmailNotificationDTO dto) {
        Notification notification = new Notification();

        notification.setChannel(NotificationChannel.EMAIL);
        notification.setRecipient(dto.getTo());
        notification.setSubject(dto.getSubject());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.PENDING);

        repo.save(notification);

        EmailNotificationMessage message =
                new EmailNotificationMessage(
                        notification.getId(),
                        dto.getTo(),
                        dto.getSubject(),
                        dto.getContent()
        );

        producer.publishEmail(message);
    }
}
