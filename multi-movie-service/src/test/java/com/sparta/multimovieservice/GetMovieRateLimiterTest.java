package com.sparta.multimovieservice;

import com.sparta.multimovieservice.redis.GetMovieRateLimiter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GetMovieRateLimiterTest {
    @Autowired
    private GetMovieRateLimiter rateLimiter;

    @AfterEach
    void tearDown() {
        rateLimiter.clearLimit("127.0.0.1");
        rateLimiter.clearLimit("127.0.0.2");
        rateLimiter.clearLimit("127.0.0.3");
    }

    @Test
    @DisplayName("IP당 1분에 50회까지 조회 가능")
    void screeningRateLimitBasicTest() {
        String ip = "127.0.0.1";

        for (int i = 0; i < 50; i++) {
            assertTrue(rateLimiter.checkScreeningRateLimit(ip));
        }

        assertFalse(rateLimiter.checkScreeningRateLimit(ip));
    }

    @Test
    @DisplayName("IP가 null인 경우 예외 발생")
    void screeningRateLimitNullIpTest() {
        assertThrows(IllegalArgumentException.class, () ->
                rateLimiter.checkScreeningRateLimit(null)
        );
    }

    @Test
    @DisplayName("다른 IP는 각각 50회까지 가능")
    void screeningRateLimitDifferentIpsTest() {
        String ip1 = "127.0.0.1";
        String ip2 = "127.0.0.2";

        for (int i = 0; i < 50; i++) {
            assertTrue(rateLimiter.checkScreeningRateLimit(ip1));
        }
        assertFalse(rateLimiter.checkScreeningRateLimit(ip1));

        for (int i = 0; i < 50; i++) {
            assertTrue(rateLimiter.checkScreeningRateLimit(ip2),
                    "Request " + i + " for IP2 should succeed");
        }
    }
}
