package com.ofentse.pulse.exception;

import lombok.Getter;

@Getter
public class EmailNotFoundException extends RuntimeException{
    private final String field;
    public EmailNotFoundException(String field, String message) {
        super(message);
        this.field = field;
    }

}
