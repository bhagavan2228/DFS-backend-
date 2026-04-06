package com.fourm.discussion_forum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Let Spring's ResponseStatusException carry its own HTTP status code.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getReason());
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    /**
     * Catch-all for unchecked exceptions the controllers didn't wrap
     * in ResponseStatusException.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        String message = ex.getMessage();
        response.put("message", message != null ? message : "Internal server error");

        // If the error message contains 'rejected' or 'toxic', return 400 Bad Request
        if (message != null && (message.toLowerCase().contains("rejected") || message.toLowerCase().contains("toxic"))) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Generic 500 for other runtime exceptions
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
