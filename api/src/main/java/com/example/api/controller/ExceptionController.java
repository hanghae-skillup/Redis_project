package com.example.api.controller;

import com.example.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<?> handleBadCredentialsException(Exception e, HttpServletRequest request) throws IOException {
        e.printStackTrace();
        ErrorResponse errorResponse = new ErrorResponse(
                "400",
                "Bad Request",
                e.getMessage(),
                request.getRequestURL());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}
