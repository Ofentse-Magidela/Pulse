package com.ofentse.pulse.notification.sms;

import com.ofentse.pulse.notification.sms.dto.SmsNotificationMessage;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    public void sendSms(SmsNotificationMessage message) {
        System.out.println("Mock sms to " + message.getTo() + ": " + message.getContent());
    }
}
