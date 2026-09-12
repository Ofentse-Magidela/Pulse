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
import com.ofentse.pulse.notification.template.NotificationTemplate;
import com.ofentse.pulse.notification.template.NotificationTemplateService;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationDTO;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;


@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepo repo;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepo outboxRepo;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final NotificationTemplateService notificationTemplateService;

    public NotificationService(NotificationRepo repo, ObjectMapper objectMapper, OutboxEventRepo outboxRepo,
                               ApplicationEventPublisher applicationEventPublisher, NotificationTemplateService notificationTemplateService) {
        this.repo = repo;
        this.objectMapper = objectMapper;
        this.outboxRepo = outboxRepo;
        this.applicationEventPublisher = applicationEventPublisher;
        this.notificationTemplateService = notificationTemplateService;
    }

    @Transactional
    public void sendEmailNotification(EmailNotificationDTO dto) {

        NotificationTemplate template = notificationTemplateService.getTemplate(dto.getTemplateName());

        String subject = notificationTemplateService.render(template.getSubject(), dto.getVariables());
        String content = notificationTemplateService.render(template.getBody(), dto.getVariables());

        Notification notification = new Notification();

        notification.setChannel(NotificationChannel.EMAIL);
        notification.setRecipient(dto.getTo());
        notification.setSubject(subject);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.PENDING);

        repo.save(notification);

        log.info("Email notification {} created with status PENDING", notification.getId());

        EmailNotificationMessage message =
                new EmailNotificationMessage(
                        notification.getId(),
                        dto.getTo(),
                        subject,
                        content
        );

        saveOutboxEvent(message, notification);

        applicationEventPublisher.publishEvent(new OutboxEventCreated());

        log.info("OutboxEventCreated published for notification {}", notification.getId());
    }

    @Transactional
    public void sendWhatsAppNotification(WhatsAppNotificationDTO dto) {
        Notification notification = new Notification();

        notification.setChannel(NotificationChannel.WHATSAPP);
        notification.setRecipient(dto.getTo());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.PENDING);

        repo.save(notification);

        log.info("WhatsApp notification {} created with status PENDING", notification.getId());

        WhatsAppNotificationMessage message = new WhatsAppNotificationMessage(
                notification.getId(),
                dto.getTo(),
                dto.getContent()
        );

        saveOutboxEvent(message, notification);

        applicationEventPublisher.publishEvent(new OutboxEventCreated());

        log.info("OutboxEventCreated published for notification {}", notification.getId());
    }

    private <T> void saveOutboxEvent(T message, Notification notification) {

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

        log.info(
                "Outbox event {} created for notification {} with status PENDING", outbox.getId(), notification.getId()
        );
    }
}