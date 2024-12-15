package com.example.service.event;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class ReservationCompletedEvent extends ApplicationEvent {
    private final Long reservationId;// 예약 ID
    private final Long userId;        // 예약한 사용자 ID
    private final Long movieId;       // 예약된 영화 ID
    private final List<String> seatNumbers; // 예약된 좌석 번호 목록

    public ReservationCompletedEvent(Object source, Long reservationId,
                                     Long userId, Long movieId, List<String> seatNumbers) {
        super(source);
        this.reservationId = reservationId;
        this.userId = userId;
        this.movieId = movieId;
        this.seatNumbers = seatNumbers;
    }
}
