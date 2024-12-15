package com.sparta.multimovieservice.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class GetMovieRateLimiter {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String SCREENING_RATE_LIMIT_KEY = "rate:screening:";

    public GetMovieRateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean checkScreeningRateLimit(String ip) {
        if (ip == null) {
            throw new IllegalArgumentException("IP cannot be null");
        }

        String key = SCREENING_RATE_LIMIT_KEY + ip;

        // 초기값 설정
        Long count = redisTemplate.opsForValue().increment(key, 1);
        if (count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }

        // 50회 초과시
        if (count > 50) {
            // 1시간 차단으로 변경
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
            return false;
        }

        return true;
    }

    // 테스트용 메서드
    public void clearLimit(String ip) {
        String key = SCREENING_RATE_LIMIT_KEY + ip;
        redisTemplate.delete(key);
    }
}