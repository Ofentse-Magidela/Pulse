package com.ofentse.pulse.notification.producer;

import com.ofentse.pulse.notification.config.RabbitMQConfig;
import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationProducer {
    private final RabbitTemplate rabbitTemplate;
    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEmail(EmailNotificationMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                message
        );
    }

    public void publishWhatsApp(WhatsAppNotificationMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.WHATSAPP_ROUTING_KEY,
                message
        );
    }
}
