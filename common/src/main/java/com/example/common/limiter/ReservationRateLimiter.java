package com.example.common.limiter;

import com.example.common.exception.RateLimitExceededException;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReservationRateLimiter {
    private final Map<String, RateLimiter> reservationLimiters = new ConcurrentHashMap<>();

    /**
     * 유저가 동일한 시간대의 영화를 5분에 1번만 예약 가능
     * key 형식: "userId:movieId:timeSlot"
     */
    public void checkRateLimitForReservation(Long userId, Long movieId, String timeSlot) {
        // 5분에 1회 허용 -> 초당 약 1/300
        String key = userId + ":" + movieId + ":" + timeSlot;
        RateLimiter limiter = reservationLimiters.computeIfAbsent(key, k -> RateLimiter.create(1.0/300.0));

        if (!limiter.tryAcquire()) {
            throw new RateLimitExceededException("해당 시간대에 이미 예약하셨습니다. 5분 후 다시 시도해주세요.");
        }
    }
}