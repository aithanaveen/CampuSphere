package com.smartcampus.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler for the Smart Campus API.
 * Converts RuntimeExceptions into clean JSON error responses
 * instead of exposing raw Spring stack traces to the client.
 *
 * Format: { "message": "...", "status": 400 }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all domain-level RuntimeExceptions thrown from services.
     * Examples: Student not found, Event not found, Event is full,
     * Duplicate registration, Invalid rating.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles Spring Security access denial (403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        return buildResponse("Access denied: you do not have permission to perform this action.",
                HttpStatus.FORBIDDEN);
    }

    /**
     * Catch-all for unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildResponse("An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(String message, HttpStatus status) {
        Map<String, Object> body = Map.of(
                "message", message != null ? message : "Unknown error",
                "status", status.value()
        );
        return new ResponseEntity<>(body, status);
    }
}
