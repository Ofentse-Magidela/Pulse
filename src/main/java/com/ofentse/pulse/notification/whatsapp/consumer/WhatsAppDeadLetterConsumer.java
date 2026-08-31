package com.ofentse.pulse.notification.whatsapp.consumer;

import com.ofentse.pulse.notification.config.RabbitMQConfig;
import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.exception.NotificationNotFoundException;
import com.ofentse.pulse.notification.repository.NotificationRepo;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppDeadLetterConsumer {

    private final NotificationRepo notificationRepo;
    public WhatsAppDeadLetterConsumer(NotificationRepo notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    @RabbitListener(queues = RabbitMQConfig.WHATSAPP_DLQ)
    public void consumeDeadLetterWhatsApp(WhatsAppNotificationMessage message) {

        Notification notification = notificationRepo.findById(message.getNotificationId())
                .orElseThrow(
                        ()-> new NotificationNotFoundException(
                                "notification", "Notification with ID: " + message.getNotificationId() + " not found.")
                );

        if (notification.getStatus() == NotificationStatus.FAILED) return;

        notification.setStatus(NotificationStatus.FAILED);
        notificationRepo.save(notification);
    }
}
