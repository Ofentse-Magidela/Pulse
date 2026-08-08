package com.ofentse.pulse.exception;

import lombok.Getter;

@Getter
public class InvalidTokenException extends RuntimeException{

    private final String field;
    public InvalidTokenException(String field, String message) {
        super(message);
        this.field = field;
    }
}
