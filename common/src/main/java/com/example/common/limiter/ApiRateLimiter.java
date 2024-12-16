package com.example.common.limiter;



import com.example.common.exception.RateLimitExceededException;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiRateLimiter {
    private final Map<String, RateLimiter> ipLimiters = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedIps = new ConcurrentHashMap<>();

    // 1분 내 50회 이상 -> 1시간 차단
    public void checkRateLimitForIp(String ip) {
        // 차단 여부 확인
        Long blockedUntil = blockedIps.get(ip);
        if (blockedUntil != null && System.currentTimeMillis() < blockedUntil) {
            throw new RateLimitExceededException("해당 IP는 1시간 차단 상태입니다.");
        } else if (blockedUntil != null) {
            // 차단기간 지났으니 해제
            blockedIps.remove(ip);
        }

        RateLimiter limiter = ipLimiters.computeIfAbsent(ip, k -> RateLimiter.create(50.0/60.0));
        if (!limiter.tryAcquire()) {
            // 토큰 획득 실패 -> 차단
            blockedIps.put(ip, System.currentTimeMillis() + 3600_000L);
            throw new RateLimitExceededException("1분 내 50회 초과 요청으로 1시간 차단되었습니다.");
        }
    }
}
