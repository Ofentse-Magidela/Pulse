package com.ofentse.pulse.notification.exception;

import lombok.Getter;

@Getter
public class NotificationNotFoundException extends RuntimeException {
    private final String field;
    public NotificationNotFoundException(String field, String message) {
        super(message);
        this.field = field;
    }
}
