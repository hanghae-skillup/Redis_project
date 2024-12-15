package com.sparta.multibookingservice.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class BookingRateLimiter {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BOOKING_RATE_LIMIT_KEY = "rate:booking:";

    public BookingRateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean checkBookingRateLimit(String userId, Integer screeningId) {
        if (userId == null || screeningId == null) {
            throw new IllegalArgumentException("UserId and screeningId cannot be null");
        }

        String key = BOOKING_RATE_LIMIT_KEY + userId + ":" + screeningId;
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(key, "1", 5, TimeUnit.MINUTES));
    }

    public void clearLimit(String userId, Integer screeningId) {
        String key = BOOKING_RATE_LIMIT_KEY + userId + ":" + screeningId;
        redisTemplate.delete(key);
    }
}