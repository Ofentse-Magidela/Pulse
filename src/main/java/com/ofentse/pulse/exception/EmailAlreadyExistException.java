package com.ofentse.pulse.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyExistException extends RuntimeException{
    private final String field;
    public EmailAlreadyExistException(String field, String message) {
        super(message);
        this.field = field;
    }

}
