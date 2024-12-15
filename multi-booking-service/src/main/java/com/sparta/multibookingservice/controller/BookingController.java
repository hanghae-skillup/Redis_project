package com.sparta.multibookingservice.controller;

import com.sparta.dto.booking.BookingRequestDto;
import com.sparta.dto.booking.BookingResponseDto;
import com.sparta.multibookingservice.exception.RateLimitException;
import com.sparta.multibookingservice.redis.BookingRateLimiter;
import com.sparta.multibookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final BookingRateLimiter bookingRateLimiter;

    @PostMapping
    public ResponseEntity<List<BookingResponseDto>> book(@Valid @RequestBody BookingRequestDto request) {
        if (!bookingRateLimiter.checkBookingRateLimit(request.userId(), request.screeningId())) {
            throw new RateLimitException("Can only book once every 5 minutes for the same screening.");
        }

        List<BookingResponseDto> response = bookingService.book(request);
        return ResponseEntity.ok(response);
    }
}