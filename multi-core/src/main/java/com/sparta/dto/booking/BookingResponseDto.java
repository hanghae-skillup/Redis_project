package com.sparta.dto.booking;

import com.sparta.domain.booking.Booking;

import java.time.LocalDateTime;

public record BookingResponseDto(
        Integer id,
        String movieTitle,
        String theaterName,
        String seatNumber,
        LocalDateTime startTime,
        String userId,
        LocalDateTime createdAt
) {
    public static BookingResponseDto from(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getScreening().getMovie().getTitle(),
                booking.getScreening().getTheater().getName(),
                booking.getSeat().getSeatRow() + booking.getSeat().getSeatColumn(),
                booking.getScreening().getStartTime(),
                booking.getUserId(),
                booking.getCreatedAt()
        );
    }
}
