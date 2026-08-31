package com.ofentse.pulse.notification.service;

import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.entity.OutboxEvent;
import com.ofentse.pulse.notification.enums.NotificationChannel;
import com.ofentse.pulse.notification.producer.NotificationProducer;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
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

    // Push switch left
    public void publishEvent(OutboxEvent event) {

        try {
            NotificationChannel channel = event.getNotification().getChannel();

            switch(channel) {

                case EMAIL ->  {
                    EmailNotificationMessage message = objectMapper.readValue(
                            event.getPayload(), EmailNotificationMessage.class
                    );

                    producer.publishEmail(message);
                }
                case WHATSAPP -> {
                    WhatsAppNotificationMessage message = objectMapper.readValue(
                            event.getPayload(), WhatsAppNotificationMessage.class
                    );

                    producer.publishWhatsApp(message);
                }
            }

            outboxStateService.markPublished(event);

        } catch (Exception e) {

            outboxStateService.recordFailure(event, e);
        }
    }
}
