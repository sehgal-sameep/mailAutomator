package com.codewithsam.mailautomator.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, duplicate) -> existing,
                        LinkedHashMap::new));
        log.warn("Request validation failed: {}", fieldErrors);
        return Map.of("error", "Validation failed", "fieldErrors", fieldErrors);
    }

    @ExceptionHandler(GoogleSheetReadException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> handleGoogleSheetReadException(GoogleSheetReadException ex) {
        log.error("Google Sheet read failed: {}", ex.getMessage());
        return Map.of("error", "Google Sheet read failed", "message", ex.getMessage());
    }

    @ExceptionHandler(EmailSendException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleEmailSendException(EmailSendException ex) {
        log.error("Email send failed: {}", ex.getMessage());
        return Map.of("error", "Email send failed", "message", ex.getMessage());
    }

    @ExceptionHandler(TemplateRenderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleTemplateRenderException(TemplateRenderException ex) {
        log.error("Template render failed: {}", ex.getMessage());
        return Map.of("error", "Template render failed", "message", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> handleIllegalState(IllegalStateException ex) {
        log.error("Configuration error: {}", ex.getMessage());
        return Map.of("error", "Configuration error", "message", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return Map.of("error", "Internal server error", "message", ex.getMessage());
    }
}
