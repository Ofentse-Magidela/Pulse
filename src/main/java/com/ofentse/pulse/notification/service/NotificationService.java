package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.email.dto.EmailNotificationDTO;
import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.NotificationChannel;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import com.ofentse.pulse.notification.event.OutboxEventCreated;
import com.ofentse.pulse.notification.repository.NotificationRepo;
import com.ofentse.pulse.notification.repository.OutboxEventRepo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationRepo repo;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepo outboxRepo;
    private final ApplicationEventPublisher applicationEventPublisher;
    public NotificationService(NotificationRepo repo, ObjectMapper objectMapper, OutboxEventRepo outboxRepo,
                               ApplicationEventPublisher applicationEventPublisher) {
        this.repo = repo;
        this.objectMapper = objectMapper;
        this.outboxRepo = outboxRepo;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
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

        String payload = objectMapper.writeValueAsString(message);

        OutboxEvent outbox = new OutboxEvent();
        LocalDateTime now = LocalDateTime.now();

        outbox.setNotification(notification);
        outbox.setPayload(payload);
        outbox.setStatus(OutboxEventStatus.PENDING);
        outbox.setCreatedAt(now);
        outbox.setRetryCount(0);
        outbox.setNextRetryAt(now);

        outboxRepo.save(outbox);

        applicationEventPublisher.publishEvent(new OutboxEventCreated());
    }
}
