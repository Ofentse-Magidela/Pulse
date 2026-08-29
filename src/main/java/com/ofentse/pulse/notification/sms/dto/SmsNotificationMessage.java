package com.ofentse.pulse.notification.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SmsNotificationMessage {

    private Long notificationId;
    private String to;
    private String content;
}
