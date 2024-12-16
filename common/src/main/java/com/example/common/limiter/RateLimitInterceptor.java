package com.example.common.limiter;


import com.example.common.exception.RateLimitExceededException;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;


public class RateLimitInterceptor implements HandlerInterceptor {

    private final ApiRateLimiter apiRateLimiter;
    private final ReservationRateLimiter reservationRateLimiter;

    public RateLimitInterceptor(ApiRateLimiter apiRateLimiter, ReservationRateLimiter reservationRateLimiter) {
        this.apiRateLimiter = apiRateLimiter;
        this.reservationRateLimiter = reservationRateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();

        if (uri.startsWith("/movies/search")) {
            // 조회 API는 IP 기반 Rate Limit
            String clientIp = getClientIp(request);
            apiRateLimiter.checkRateLimitForIp(clientIp);

        } else if (uri.startsWith("/reservations")) {
            // 예약 API는 User + 시간대 기반 Rate Limit 적용
            // 예: userId, movieId, timeSlot 획득 (여기선 예시로 param에서 가정)
            Long userId = getUserIdFromAuth(); // 실제 구현 필요 (JWT 토큰에서 추출 등)
            Long movieId = Long.valueOf(request.getParameter("movieId")); // 예: 파라미터로부터
            String timeSlot = request.getParameter("timeSlot"); // 예: 파라미터로부터
            reservationRateLimiter.checkRateLimitForReservation(userId, movieId, timeSlot);
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private Long getUserIdFromAuth() {
        // JWT나 SecurityContextHolder에서 유저 ID 추출 로직 구현 필요
        // 여기서는 예시로 하드코딩
        return 1L;
    }
}
