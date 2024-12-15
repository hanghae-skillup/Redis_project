package com.sparta.multibookingservice;

import com.sparta.dto.booking.BookingRequestDto;
import com.sparta.dto.booking.BookingResponseDto;
import com.sparta.multibookingservice.controller.BookingController;
import com.sparta.multibookingservice.exception.RateLimitException;
import com.sparta.multibookingservice.redis.BookingRateLimiter;
import com.sparta.multibookingservice.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookLimiterTest {

    @InjectMocks
    private BookingController bookingController;

    @Mock
    private BookingService bookingService;

    @Mock
    private BookingRateLimiter bookingRateLimiter;

    private BookingRequestDto bookingRequest;

    @BeforeEach
    void setUp() {
        bookingRequest = new BookingRequestDto(
                "user1",
                1,
                "010-1234-5678",
                Arrays.asList("A1", "A2")
        );
    }

    @Test
    @DisplayName("Rate Limit 통과 - 첫 번째 예약 성공")
    void shouldAllowBookingWhenRateLimitNotExceeded() {
        // Given
        when(bookingRateLimiter.checkBookingRateLimit(bookingRequest.userId(), bookingRequest.screeningId()))
                .thenReturn(true); // Rate limit allows booking
        List<BookingResponseDto> responseDtoList = Arrays.asList(
                new BookingResponseDto(1, "Test Movie", "Theater A", "A1", null, "user1", null),
                new BookingResponseDto(2, "Test Movie", "Theater A", "A2", null, "user1", null)
        );
        when(bookingService.book(bookingRequest)).thenReturn(responseDtoList);

        // When
        ResponseEntity<List<BookingResponseDto>> response = bookingController.book(bookingRequest);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).seatNumber()).isEqualTo("A1");
        assertThat(response.getBody().get(1).seatNumber()).isEqualTo("A2");

        verify(bookingRateLimiter, times(1))
                .checkBookingRateLimit(bookingRequest.userId(), bookingRequest.screeningId());
        verify(bookingService, times(1)).book(bookingRequest);
    }

    @Test
    @DisplayName("Rate Limit - 5분 이내 다시 예약 시 RateLimitException 발생")
    void shouldThrowRateLimitExceptionWhenRateLimitExceeded() {
        // Given
        when(bookingRateLimiter.checkBookingRateLimit(bookingRequest.userId(), bookingRequest.screeningId()))
                .thenReturn(false); // Rate limit prevents booking

        // When & Then
        assertThatThrownBy(() -> bookingController.book(bookingRequest))
                .isInstanceOf(RateLimitException.class)
                .hasMessageContaining("Can only book once every 5 minutes for the same screening.");

        verify(bookingRateLimiter, times(1))
                .checkBookingRateLimit(bookingRequest.userId(), bookingRequest.screeningId());
        verifyNoInteractions(bookingService);
    }

    @Test
    @DisplayName("Rate Limit이 허용될 때 예약 성공 - Controller 200 OK 응답")
    void shouldReturn200OkWhenBookingIsSuccessful() {
        // Given
        when(bookingRateLimiter.checkBookingRateLimit(bookingRequest.userId(), bookingRequest.screeningId()))
                .thenReturn(true);
        List<BookingResponseDto> responseDtoList = Arrays.asList(
                new BookingResponseDto(1, "Test Movie", "Theater A", "A1", null, "user1", null),
                new BookingResponseDto(2, "Test Movie", "Theater A", "A2", null, "user1", null)
        );
        when(bookingService.book(bookingRequest)).thenReturn(responseDtoList);

        // When
        ResponseEntity<List<BookingResponseDto>> response = bookingController.book(bookingRequest);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).seatNumber()).isEqualTo("A1");
        assertThat(response.getBody().get(1).seatNumber()).isEqualTo("A2");

        verify(bookingRateLimiter, times(1))
                .checkBookingRateLimit(bookingRequest.userId(), bookingRequest.screeningId());
        verify(bookingService, times(1)).book(bookingRequest);
    }

    @Test
    @DisplayName("RateLimiter 호출")
    void rateLimiterShouldAlwaysBeCalled() {
        // Given
        when(bookingRateLimiter.checkBookingRateLimit(bookingRequest.userId(), bookingRequest.screeningId()))
                .thenReturn(true);

        // When
        bookingController.book(bookingRequest);

        // Then
        verify(bookingRateLimiter, times(1))
                .checkBookingRateLimit(bookingRequest.userId(), bookingRequest.screeningId());
    }
}