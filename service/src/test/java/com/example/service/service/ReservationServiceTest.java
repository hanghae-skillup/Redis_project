package com.example.service.service;

import com.example.common.dto.ReservationRequest;
import com.example.common.lock.DistributedLockExecutor;
import com.example.domain.entity.Movies;
import com.example.domain.entity.Reservations;
import com.example.domain.entity.Seats;
import com.example.domain.repository.MovieRepository;
import com.example.domain.repository.ReservationRepository;
import com.example.domain.repository.SeatRepository;
import com.example.service.event.ReservationCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private DistributedLockExecutor lockExecutor;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationRequest request;

    @BeforeEach
    void setup() {
        request = ReservationRequest.builder()
                .userId(1L)
                .movieId(100L)
                .seatNumbers(List.of("A1", "A2"))
                .build();
    }

    @Test
    void testMovieNotFound() {
        // movie 존재하지 않을 때
        when(movieRepository.findById(request.getMovieId()))
                .thenReturn(Optional.empty());

        doAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(2);
            return supplier.get();
        }).when(lockExecutor).executeWithLock(anyString(), anyLong(), any());

        assertThatThrownBy(() -> reservationService.reserveSeats(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("영화가 존재하지 않습니다.");
    }

    @Test
    void testMovieNotShowing() {
        Movies notShowingMovie = mock(Movies.class);
        when(notShowingMovie.notShowing()).thenReturn(true);
        when(movieRepository.findById(request.getMovieId())).thenReturn(Optional.of(notShowingMovie));

        doAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(2);
            return supplier.get();
        }).when(lockExecutor).executeWithLock(anyString(), anyLong(), any());
        assertThatThrownBy(() -> reservationService.reserveSeats(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상영중인 영화가 아닙니다.");
    }

    @Test
    void testSeatsNotExist() {
        Movies showingMovie = mock(Movies.class);
        when(showingMovie.notShowing()).thenReturn(false);
        when(showingMovie.getId()).thenReturn(request.getMovieId());
        when(movieRepository.findById(request.getMovieId())).thenReturn(Optional.of(showingMovie));

        // createSeatsIfNotExists 호출 후에도 좌석 조회 결과 없음
        when(seatRepository.existsByMovieId(request.getMovieId())).thenReturn(false);
        when(seatRepository.findByMovieIdAndSeatNumberIn(request.getMovieId(), request.getSeatNumbers())).thenReturn(List.of());

        doAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(2);
            return supplier.get();
        }).when(lockExecutor).executeWithLock(anyString(), anyLong(), any());
        assertThatThrownBy(() -> reservationService.reserveSeats(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("좌석이 존재하지 않습니다.");
    }

    @Test
    void testAlreadyHave5SeatsAndTryReserveMore() {
        Movies showingMovie = mock(Movies.class);
        when(showingMovie.notShowing()).thenReturn(false);
        // movie.getId() 호출 시 request.getMovieId() 반환하도록 설정
        when(showingMovie.getId()).thenReturn(request.getMovieId());

        when(movieRepository.findById(request.getMovieId())).thenReturn(Optional.of(showingMovie));

        // 기초 좌석 존재한다고 가정
        when(seatRepository.existsByMovieId(request.getMovieId())).thenReturn(true);
        // 예약하려는 좌석 2개는 존재하고 예약되지 않음
        Seats seatA1 = Seats.builder().movieId(request.getMovieId()).seatNumber("A1").isReserved(false).build();
        Seats seatA2 = Seats.builder().movieId(request.getMovieId()).seatNumber("A2").isReserved(false).build();
        when(seatRepository.findByMovieIdAndSeatNumberIn(request.getMovieId(), request.getSeatNumbers()))
                .thenReturn(List.of(seatA1, seatA2));

        // 이미 해당 유저가 4개의 좌석을 예약했다고 가정 (총합 6개 >5)
        Seats alreadyReservedSeat = Seats.builder().movieId(request.getMovieId()).seatNumber("A3").isReserved(true).build();
        when(seatRepository.findSeatsByMovieIdAndUserId(request.getMovieId(), request.getUserId()))
                .thenReturn(List.of(alreadyReservedSeat, alreadyReservedSeat, alreadyReservedSeat, alreadyReservedSeat));

        // executeWithLock 메서드에서 람다 실행되도록 설정
        doAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(2);
            return supplier.get();
        }).when(lockExecutor).executeWithLock(anyString(), anyLong(), any());

        assertThatThrownBy(() -> reservationService.reserveSeats(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예매 가능한 좌석은 5개 입니다.");

    }

    @Test
    void testSeatAlreadyReserved() {
        Movies showingMovie = mock(Movies.class);
        when(showingMovie.notShowing()).thenReturn(false);
        // movie.getId() 호출 시 request.getMovieId()를 반환하도록 설정
        when(showingMovie.getId()).thenReturn(request.getMovieId());

        when(movieRepository.findById(request.getMovieId())).thenReturn(Optional.of(showingMovie));

        // 좌석 2개 존재, 그 중 하나는 이미 예약됨
        Seats seatA1 = Seats.builder().movieId(request.getMovieId()).seatNumber("A1").isReserved(true).build();
        Seats seatA2 = Seats.builder().movieId(request.getMovieId()).seatNumber("A2").isReserved(false).build();
        when(seatRepository.existsByMovieId(request.getMovieId())).thenReturn(true);
        when(seatRepository.findByMovieIdAndSeatNumberIn(request.getMovieId(), request.getSeatNumbers()))
                .thenReturn(List.of(seatA1, seatA2));
        when(seatRepository.findSeatsByMovieIdAndUserId(request.getMovieId(), request.getUserId()))
                .thenReturn(List.of()); // 기존 예약 없음

        doAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(2);
            return supplier.get();
        }).when(lockExecutor).executeWithLock(anyString(), anyLong(), any());

        assertThatThrownBy(() -> reservationService.reserveSeats(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 예약된 좌석이 포함되어 있습니다.");    }

    @Test
    void testNotContinuousSeats() {
        Movies showingMovie = mock(Movies.class);
        when(showingMovie.notShowing()).thenReturn(false);
        when(showingMovie.getId()).thenReturn(request.getMovieId()); // getId() 설정 추가
        when(movieRepository.findById(request.getMovieId())).thenReturn(Optional.of(showingMovie));

        // A1, A3 요청 : 연속되지 않음
        request.updateSeatNumbers(List.of("A1", "A3"));

        Seats seatA1 = Seats.builder().movieId(request.getMovieId()).seatNumber("A1").isReserved(false).build();
        Seats seatA3 = Seats.builder().movieId(request.getMovieId()).seatNumber("A3").isReserved(false).build();

        when(seatRepository.existsByMovieId(request.getMovieId())).thenReturn(true);
        when(seatRepository.findByMovieIdAndSeatNumberIn(request.getMovieId(), request.getSeatNumbers()))
                .thenReturn(List.of(seatA1, seatA3));
        when(seatRepository.findSeatsByMovieIdAndUserId(request.getMovieId(), request.getUserId()))
                .thenReturn(List.of());
        doAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(2);
            return supplier.get();
        }).when(lockExecutor).executeWithLock(anyString(), anyLong(), any());

        assertThatThrownBy(() -> reservationService.reserveSeats(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("좌석은 연속된 형태로만 예약 가능합니다.");
    }

    @Test
    void testSuccessScenario() {
        Movies showingMovie = mock(Movies.class);
        when(showingMovie.getId()).thenReturn(request.getMovieId());
        when(showingMovie.notShowing()).thenReturn(false);
        when(movieRepository.findById(request.getMovieId())).thenReturn(Optional.of(showingMovie));

        // 요청한 좌석 A1, A2 존재, 예약 안됨
        Seats seatA1 = Seats.builder().movieId(request.getMovieId()).seatNumber("A1").isReserved(false).build();
        Seats seatA2 = Seats.builder().movieId(request.getMovieId()).seatNumber("A2").isReserved(false).build();
       when(seatRepository.existsByMovieId(request.getMovieId())).thenReturn(true);
        when(seatRepository.findByMovieIdAndSeatNumberIn(request.getMovieId(), request.getSeatNumbers()))
                .thenReturn(List.of(seatA1, seatA2));
        when(seatRepository.findSeatsByMovieIdAndUserId(request.getMovieId(), request.getUserId()))
                .thenReturn(List.of());

        Reservations savedReservation = Reservations.builder()
                .userId(request.getUserId())
                .movieId(request.getMovieId())
                .reservedDate(LocalDate.now())
                .build();
        when(reservationRepository.save(any())).thenReturn(savedReservation);

        // 락 실행 로직 mocking
        doAnswer(invocation -> {
            // 세 번째 인자는 Supplier<T> 이므로 Supplier로 캐스팅
            Supplier<?> supplier = invocation.getArgument(2);
            return supplier.get(); // supplier의 get() 호출로 예약 로직 실행
        }).when(lockExecutor).executeWithLock(anyString(), anyLong(), any());

        reservationService.reserveSeats(request);

        verify(eventPublisher, times(1)).publishEvent(any(ReservationCompletedEvent.class));
        verify(seatRepository, times(1)).saveAll(anyList());
        verify(reservationRepository, times(1)).save(any(Reservations.class));
    }

}
