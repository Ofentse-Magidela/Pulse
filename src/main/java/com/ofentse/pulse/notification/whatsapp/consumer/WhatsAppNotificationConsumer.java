package com.ofentse.pulse.notification.whatsapp.consumer;

import com.ofentse.pulse.notification.config.RabbitMQConfig;
import com.ofentse.pulse.notification.whatsapp.WhatsAppService;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppNotificationConsumer {

    private final WhatsAppService whatsAppService;

    public WhatsAppNotificationConsumer(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @RabbitListener(queues = RabbitMQConfig.WHATSAPP_QUEUE)
    public void consumeWhatsApp(WhatsAppNotificationMessage message) {
        whatsAppService.sendWhatsApp(message);
    }
}
