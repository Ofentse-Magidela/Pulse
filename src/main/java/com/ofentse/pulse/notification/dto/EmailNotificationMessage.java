package com.ofentse.pulse.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailNotificationMessage {

    private Long notificationId;
    private String to;
    private String subject;
    private String content;
}
