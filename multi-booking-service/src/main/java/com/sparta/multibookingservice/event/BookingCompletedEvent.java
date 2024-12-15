package com.sparta.multibookingservice.event;

import com.sparta.dto.booking.BookingResponseDto;

import java.util.List;

public record BookingCompletedEvent(String userId, String phoneNumber, List<BookingResponseDto> bookings) {
}