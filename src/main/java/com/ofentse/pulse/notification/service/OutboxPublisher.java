package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.OutboxEventStatus;
import com.ofentse.pulse.notification.producer.NotificationProducer;
import com.ofentse.pulse.notification.repository.OutboxEventRepo;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
public class OutboxPublisher {
    private final OutboxEventRepo outboxRepo;
    private final NotificationProducer producer;
    private final ObjectMapper objectMapper;

    //Event or payload to RabbitMq
    public OutboxPublisher(OutboxEventRepo outboxRepo, NotificationProducer producer, ObjectMapper objectMapper) {
        this.outboxRepo = outboxRepo;
        this.producer = producer;
        this.objectMapper = objectMapper;
    }

    // Will add switches for sms and push
    public void publishEvent(OutboxEvent event) {

        EmailNotificationMessage message = objectMapper.readValue(
                event.getPayload(), EmailNotificationMessage.class
        );

        producer.publishEmail(message);

        event.setStatus(OutboxEventStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());

        outboxRepo.save(event);
    }
}
