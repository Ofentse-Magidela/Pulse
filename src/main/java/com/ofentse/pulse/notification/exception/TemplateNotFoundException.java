package com.ofentse.pulse.notification.exception;

import lombok.Getter;

@Getter
public class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException(String message) {
        super(message);
    }
}
