package com.ofentse.pulse.notification.whatsapp.consumer;

import com.ofentse.pulse.notification.config.RabbitMQConfig;
import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.exception.NotificationNotFoundException;
import com.ofentse.pulse.notification.repository.NotificationRepo;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppDeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppDeadLetterConsumer.class);

    private final NotificationRepo notificationRepo;
    public WhatsAppDeadLetterConsumer(NotificationRepo notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    @RabbitListener(queues = RabbitMQConfig.WHATSAPP_DLQ)
    public void consumeDeadLetterWhatsApp(WhatsAppNotificationMessage message) {

        log.info("Processing dead-letter WhatsApp notification {}", message.getNotificationId());

        Notification notification = notificationRepo.findById(message.getNotificationId())
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "notification", "Notification with ID: " + message.getNotificationId() + " not found.")
                );

        if (notification.getStatus() == NotificationStatus.FAILED) {

            log.info("Notification {} already FAILED; skipping duplicate dead-letter message", notification.getId());
            return;
        }

        notification.setStatus(NotificationStatus.FAILED);
        notificationRepo.save(notification);

        log.warn("WhatsApp notification {} permanently failed after exhausting retries", notification.getId());
    }
}
