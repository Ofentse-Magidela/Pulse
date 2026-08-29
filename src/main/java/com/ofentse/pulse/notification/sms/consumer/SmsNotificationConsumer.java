package com.ofentse.pulse.notification.sms.consumer;

import com.ofentse.pulse.notification.config.RabbitMQConfig;
import com.ofentse.pulse.notification.sms.SmsService;
import com.ofentse.pulse.notification.sms.dto.SmsNotificationMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SmsNotificationConsumer {

    private final SmsService smsService;

    public SmsNotificationConsumer(SmsService smsService) {
        this.smsService = smsService;
    }

    @RabbitListener(queues = RabbitMQConfig.SMS_QUEUE)
    public void consumeSms(SmsNotificationMessage message) {
        smsService.sendSms(message);
    }
}
