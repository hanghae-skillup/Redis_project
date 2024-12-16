package com.example.common.limiter;



import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiRateLimiter {
    private final Map<String, RateLimiter> limiterMap = new ConcurrentHashMap<>();

    // key는 client 식별자(IP 등)
    public RateLimiter getRateLimiter(String key) {
        return limiterMap.computeIfAbsent(key, k -> RateLimiter.create(100.0 / 60.0));
        // 분당 100회 호출 허용: 초당 약 1.666...회
    }
}
