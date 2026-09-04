package com.ofentse.pulse.notification.whatsapp;

import com.ofentse.pulse.notification.entity.Notification;
import com.ofentse.pulse.notification.enums.NotificationStatus;
import com.ofentse.pulse.notification.exception.NotificationNotFoundException;
import com.ofentse.pulse.notification.repository.NotificationRepo;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    private final WhatsAppApiClient whatsAppApiClient;
    private final NotificationRepo notificationRepo;

    public WhatsAppService(WhatsAppApiClient whatsAppApiClient, NotificationRepo notificationRepo) {
        this.whatsAppApiClient = whatsAppApiClient;
        this.notificationRepo = notificationRepo;
    }

    public void sendWhatsApp(WhatsAppNotificationMessage message) {

        Notification notification = notificationRepo.findById(message.getNotificationId())
                .orElseThrow(() -> {
                            log.warn("Notification {} not found; message will be retried", message.getNotificationId());

                            return new NotificationNotFoundException(
                                    "notification", "Notification with ID: " + message.getNotificationId() + " not found.");
                        }
                );

        if (notification.getStatus() == NotificationStatus.SENT) {

            log.info("Notification {} already SENT; skipping duplicate WhatsApp message", notification.getId());
            return;
        }

        whatsAppApiClient.sendMessage(message);

        log.info("WhatsApp message successfully sent for notification {}", notification.getId());

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        notificationRepo.save(notification);
    }
}
