package com.sparta.multibookingservice;

import com.sparta.domain.booking.Booking;
import com.sparta.domain.movie.Movie;
import com.sparta.domain.movie.Screening;
import com.sparta.domain.movie.Seat;
import com.sparta.domain.theater.Theater;
import com.sparta.dto.booking.BookingRequestDto;
import com.sparta.dto.booking.BookingResponseDto;
import com.sparta.multibookingservice.repository.BookingJpaRepository;
import com.sparta.multibookingservice.repository.ScreeningJpaRepository;
import com.sparta.multibookingservice.repository.SeatJpaRepository;
import com.sparta.multibookingservice.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingJpaRepository bookingJpaRepository;
    @Mock
    private ScreeningJpaRepository screeningJpaRepository;
    @Mock
    private SeatJpaRepository seatJpaRepository;

    @InjectMocks
    private BookingService bookingService;

    private Theater theater;
    private Movie movie;
    private Screening screening;
    private List<Seat> seats;

    @BeforeEach
    void setUp() {
        theater = Theater.builder()
                .name("상영관A")
                .build();
        theater.setId(1);

        movie = Movie.builder()
                .title("테스트 영화")
                .build();

        screening = Screening.builder()
                .movie(movie)
                .theater(theater)
                .startTime(LocalDateTime.now().plusDays(1))
                .build();
        screening.setId(1);

        seats = Arrays.asList(
                createSeat("A", 1),
                createSeat("A", 2),
                createSeat("A", 3),
                createSeat("A", 4),
                createSeat("A", 5)
        );
    }

    private Seat createSeat(String row, int column) {
        return Seat.builder()
                .theater(theater)
                .seatRow(row)
                .seatColumn(column)
                .build();
    }

    @Test
    @DisplayName("연속된 좌석 예약 성공 테스트")
    void bookConsecutiveSeatsSuccess() {
        // given
        BookingRequestDto request = new BookingRequestDto(
                "user1",
                1,
                "010-1234-5678",
                Arrays.asList("A1", "A2", "A3")
        );

        when(screeningJpaRepository.findByIdWithTheaterAndMovie(1))
                .thenReturn(Optional.of(screening));

        for (int i = 0; i < 3; i++) {
            when(seatJpaRepository.findByTheaterIdAndSeatRowAndSeatColumn(
                    1,
                    "A",
                    i + 1
            )).thenReturn(Optional.of(seats.get(i)));
        }

        when(bookingJpaRepository.findByScreening(screening))
                .thenReturn(Collections.emptyList());

        // when
        List<BookingResponseDto> result = bookingService.book(request);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).seatNumber()).isEqualTo("A1");
        assertThat(result.get(1).seatNumber()).isEqualTo("A2");
        assertThat(result.get(2).seatNumber()).isEqualTo("A3");
    }

    @Test
    @DisplayName("연속되지 않은 좌석 예약 실패 테스트")
    void bookNonConsecutiveSeatsFailure() {
        // given
        BookingRequestDto request = new BookingRequestDto(
                "user2",
                1,
                "010-1234-5678",
                Arrays.asList("A1", "A3", "A5")
        );

        // 이 테스트는 좌석 연속성 검증에서 실패하므로 모킹이 필요하지 않음

        // when & then
        assertThatThrownBy(() -> bookingService.book(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Seats must be consecutive.");
    }

    @Test
    @DisplayName("이미 예약된 좌석 예약 실패 테스트")
    void bookAlreadyReservedSeatsFailure() {
        // given
        BookingRequestDto request = new BookingRequestDto(
                "user3",
                1,
                "010-1234-5678",
                Arrays.asList("A1", "A2", "A3")
        );

        when(screeningJpaRepository.findByIdWithTheaterAndMovie(1))
                .thenReturn(Optional.of(screening));

        for (int i = 0; i < 3; i++) {
            when(seatJpaRepository.findByTheaterIdAndSeatRowAndSeatColumn(
                    1,
                    "A",
                    i + 1
            )).thenReturn(Optional.of(seats.get(i)));
        }

        Booking existingBooking = Booking.builder()
                .screening(screening)
                .seat(seats.get(0))
                .userId("other-user")
                .build();

        when(bookingJpaRepository.findByScreening(screening))
                .thenReturn(List.of(existingBooking));

        // when & then
        assertThatThrownBy(() -> bookingService.book(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Seat is already booked.");
    }
}