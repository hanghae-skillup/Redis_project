package com.sparta.dto.booking;

import lombok.Getter;

import java.util.List;

public record BookingRequestDto(
        String userId,
        Integer screeningId,
        String phoneNumber,
        List<String> seatNumbers
) {}