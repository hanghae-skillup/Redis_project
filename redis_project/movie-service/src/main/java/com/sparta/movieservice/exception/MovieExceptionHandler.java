package com.sparta.movieservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class MovieExceptionHandler {

    @ExceptionHandler(MovieException.class)
    public ResponseEntity<Map<String, String>> handleMovieException(MovieException e) {
        log.error("Movie error occurred: {}", e.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Invalid Argument Exception.");
        return ResponseEntity.badRequest().body(response);
    }
}