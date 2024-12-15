package com.example.service.service;

import com.example.service.event.ReservationCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageService {

    @EventListener
    public void handleReservationCompleted(ReservationCompletedEvent event) {
        // 예약 완료 정보를 로깅
        log.info("예약 완료 - Reservation ID: {}, User ID: {}, Movie ID: {}, Seat Numbers: {}",
                event.getReservationId(), event.getUserId(), event.getMovieId(), event.getSeatNumbers());
    }
}