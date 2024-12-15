package com.example.common.dto;

import lombok.Getter;

import java.util.List;


@Getter
public class ReservationRequest {
    private Long userId;
    private Long movieId;
    private List<String> seatNumbers; // A1, A2, A3 등 좌석 번호
}
