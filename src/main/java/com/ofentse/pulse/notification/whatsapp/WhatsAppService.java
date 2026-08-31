package com.ofentse.pulse.notification.whatsapp;

import com.ofentse.pulse.notification.whatsapp.dto.WhatsAppNotificationMessage;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {
    public void sendWhatsApp(WhatsAppNotificationMessage message) {
        System.out.println("Mock whatsapp to " + message.getTo() + ": " + message.getContent());
    }
}
