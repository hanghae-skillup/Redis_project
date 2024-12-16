package com.example.common.limiter;


import com.example.common.exception.RateLimitExceededException;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;



public class RateLimitInterceptor implements HandlerInterceptor {

    private final ApiRateLimiter apiRateLimiter;

    public RateLimitInterceptor(ApiRateLimiter apiRateLimiter) {
        this.apiRateLimiter = apiRateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIp = getClientIp(request);
        RateLimiter rateLimiter = apiRateLimiter.getRateLimiter(clientIp);

        // 0초안에 토큰 소비 가능 여부 검사 (즉시 가능여부)
        if (!rateLimiter.tryAcquire(0)) {
            throw new RateLimitExceededException("요청 한도를 초과했습니다.");
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
