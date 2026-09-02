package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.NotificationChannel;
import com.ofentse.pulse.notification.producer.NotificationProducer;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxStateService outboxStateService;
    private final NotificationProducer producer;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxStateService outboxStateService, NotificationProducer producer, ObjectMapper objectMapper) {
        this.outboxStateService = outboxStateService;
        this.producer = producer;
        this.objectMapper = objectMapper;
    }

    // Push switch left
    public void publishEvent(OutboxEvent event) {

        NotificationChannel channel = event.getNotification().getChannel();

        try {

            switch(channel) {

                case EMAIL ->  {
                    EmailNotificationMessage message = objectMapper.readValue(
                            event.getPayload(), EmailNotificationMessage.class
                    );

                    log.info("Publishing email notification for outbox event {}", event.getId());

                    producer.publishEmail(message);

                    log.info("Successfully published email notification for outbox event {}", event.getId());
                }
                case WHATSAPP -> {
                    WhatsAppNotificationMessage message = objectMapper.readValue(
                            event.getPayload(), WhatsAppNotificationMessage.class
                    );

                    log.info("Publishing whatsapp notification for outbox event {}", event.getId());

                    producer.publishWhatsApp(message);

                    log.info("Successfully published whatsapp notification for outbox event {}", event.getId());
                }
            }

            outboxStateService.markPublished(event);

            log.info("Marked the outbox event {} as PUBLISHED", event.getId());

        } catch (Exception e) {

            log.warn(
                    "Failed to publish {} notification for outbox event {}; recording failure", channel,
                    event.getId(), e
            );

            outboxStateService.recordFailure(event, e);

            log.info("Recorded failed publish attempt for outbox event {}", event.getId());
        }
    }
}
