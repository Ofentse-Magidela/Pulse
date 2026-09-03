package com.ofentse.pulse.notification.whatsapp.consumer;

import com.ofentse.pulse.notification.config.RabbitMQConfig;
import com.ofentse.pulse.notification.whatsapp.WhatsAppService;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificationConsumer.class);
    private final WhatsAppService whatsAppService;

    public WhatsAppNotificationConsumer(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @RabbitListener(queues = RabbitMQConfig.WHATSAPP_QUEUE)
    public void consumeWhatsApp(WhatsAppNotificationMessage message) {

        log.info("Received WhatsApp notification {}", message.getNotificationId());

        whatsAppService.sendWhatsApp(message);
    }
}
