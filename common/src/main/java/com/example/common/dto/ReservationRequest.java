package com.example.common.dto;

import lombok.*;

import java.util.List;


@Getter @Builder @ToString
@AllArgsConstructor  @NoArgsConstructor
public class ReservationRequest {
    private Long userId;
    private Long movieId;
    private List<String> seatNumbers; // A1, A2, A3 등 좌석 번호

    public void updateUserId(Long userId) {
        this.userId = userId;
    }

    public void updateMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public void updateSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }
}
