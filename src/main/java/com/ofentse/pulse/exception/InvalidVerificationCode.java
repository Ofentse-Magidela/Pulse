package com.ofentse.pulse.exception;

import lombok.Getter;

@Getter
public class InvalidVerificationCode extends RuntimeException{

    private final String field;

    public InvalidVerificationCode(String field, String message) {
        super(message);
        this.field = field;
    }
}
