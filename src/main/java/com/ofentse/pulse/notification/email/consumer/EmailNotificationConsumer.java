package com.ofentse.pulse.notification.email.consumer;

import com.ofentse.pulse.notification.config.RabbitMQConfig;
import com.ofentse.pulse.notification.email.EmailService;
import com.ofentse.pulse.notification.email.dto.EmailNotificationMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationConsumer {
    private final EmailService emailService;
    public EmailNotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consumeEmails(EmailNotificationMessage message) {
        emailService.sendEmail(message);
    }
}
