package com.sparta.multimovieservice;

import com.sparta.multimovieservice.controller.MovieController;
import com.sparta.multimovieservice.redis.GetMovieRateLimiter;
import com.sparta.multimovieservice.service.MovieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
class MovieControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @MockBean
    private GetMovieRateLimiter rateLimiter;

    @Test
    @DisplayName("Rate Limit 내에서 영화 목록 조회 성공")
    void getMoviesWithinRateLimit() throws Exception {
        when(rateLimiter.checkScreeningRateLimit(anyString())).thenReturn(true);
        when(movieService.getCurrentMovies()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/movies")
                        .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rate Limit 초과 시 429 응답")
    void getMoviesExceedRateLimit() throws Exception {
        when(rateLimiter.checkScreeningRateLimit(anyString())).thenReturn(false);

        mockMvc.perform(get("/movies")
                        .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isTooManyRequests());
    }
}