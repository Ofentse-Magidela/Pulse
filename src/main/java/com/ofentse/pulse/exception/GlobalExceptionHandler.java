package com.ofentse.pulse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, Object> body = new HashMap<>();

        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("timestamp", LocalDateTime.now());

        Map<String, String> errors = new HashMap<>();

        for (Object error : ex.getBindingResult().getAllErrors()) {
            FieldError fieldError = (FieldError) error;

            String fieldName = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            errors.put(fieldName, message);

        }
        body.put("message", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExistExceptions(EmailAlreadyExistException ex) {
        Map<String, Object> body = new HashMap<>();

        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("timestamp", LocalDateTime.now());

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getField(), ex.getMessage());

        body.put("message", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEmailNotFoundExceptions(EmailNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();

        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("timestamp", LocalDateTime.now());

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getField(), ex.getMessage());

        body.put("message", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(InvalidVerificationCode.class)
    public ResponseEntity<Map<String, Object>> handleInvalidVerificationCodeExceptions(InvalidVerificationCode ex) {
        Map<String, Object> body = new HashMap<>();

        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("timestamp", LocalDateTime.now());

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getField(), ex.getMessage());

        body.put("message", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTokenExceptions(InvalidTokenException ex) {
        Map<String, Object> body = new HashMap<>();

        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized Attempt");
        body.put("timestamp", LocalDateTime.now());

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getField(), ex.getMessage());

        body.put("message", errors);
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadLoginException.class)
    public ResponseEntity<Map<String, Object>> handleBadLoginExceptions(BadLoginException ex) {

        Map<String, Object> body = new HashMap<>();

        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized Attempt");
        body.put("timestamp", LocalDateTime.now());

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getField(), ex.getMessage());

        body.put("message", errors);

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        Map<String, Object> body = new HashMap<>();

        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred on the server side");
        body.put("timestamp", LocalDateTime.now());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
