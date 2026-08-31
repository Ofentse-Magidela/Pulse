package com.ofentse.pulse.notification.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class WhatsAppNotificationMessage {

    private Long notificationId;
    private String to;
    private String content;
}
