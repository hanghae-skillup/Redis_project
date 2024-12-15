package com.sparta.multibookingservice.controller;

import com.sparta.dto.booking.BookingRequestDto;
import com.sparta.dto.booking.BookingResponseDto;
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

    @PostMapping
    public ResponseEntity<List<BookingResponseDto>> book(@Valid @RequestBody BookingRequestDto request) {
        List<BookingResponseDto> response = bookingService.book(request);
        return ResponseEntity.ok(response);
    }
}