package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.producer.NotificationProducer;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class OutboxPublisher {
    private final OutboxStateService outboxStateService;
    private final NotificationProducer producer;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxStateService outboxStateService, NotificationProducer producer, ObjectMapper objectMapper) {
        this.outboxStateService = outboxStateService;
        this.producer = producer;
        this.objectMapper = objectMapper;
    }

    // Will add switches for sms and push
    public void publishEvent(OutboxEvent event) {

        try{
            EmailNotificationMessage message = objectMapper.readValue(
                    event.getPayload(), EmailNotificationMessage.class
            );

            producer.publishEmail(message);

            outboxStateService.markPublished(event);

        } catch (Exception e) {

            outboxStateService.recordFailure(event, e);
        }
    }
}
