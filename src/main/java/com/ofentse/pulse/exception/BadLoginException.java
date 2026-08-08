package com.ofentse.pulse.exception;

import lombok.Getter;

@Getter
public class BadLoginException extends RuntimeException{
    private final String field;
    public BadLoginException(String field, String message) {
        super(message);
        this.field = field;
    }
}
