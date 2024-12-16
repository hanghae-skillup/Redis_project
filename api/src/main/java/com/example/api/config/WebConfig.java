package com.example.api.config;

import com.example.common.limiter.ApiRateLimiter;
import com.example.common.limiter.RateLimitInterceptor;
import com.example.common.limiter.ReservationRateLimiter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ApiRateLimiter apiRateLimiter;
    private final ReservationRateLimiter reservationRateLimiter;

    public WebConfig(ApiRateLimiter apiRateLimiter,
                     ReservationRateLimiter reservationRateLimiter) {
        this.apiRateLimiter = apiRateLimiter;
        this.reservationRateLimiter = reservationRateLimiter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 모든 요청에 대해 RateLimiter 적용
        registry.addInterceptor(new RateLimitInterceptor(apiRateLimiter, reservationRateLimiter))
                .addPathPatterns("/**");
    }
}
