package com.example.service.service;

import com.example.common.dto.ReservationRequest;
import com.example.common.enums.EGenre;
import com.example.domain.entity.Movies;
import com.example.domain.entity.Seats;
import com.example.domain.repository.MovieRepository;
import com.example.domain.repository.SeatRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.*;


@SpringBootTest
@EntityScan(basePackages = "com.example.domain.entity") // 엔티티 스캔 경로 추가
@ComponentScan(basePackages = {"com.example.service", "com.example.domain", "com.example.common"}) // 서비스, 도메인 모듈 스캔
class ReservationLockTest {


    @Autowired
    private ReservationService reservationService;

    @Autowired
    private SeatRepository seatsRepository;

    @Autowired
    private MovieRepository movieRepository;

//    @Test
//    void pessimistic_lock_test() throws InterruptedException, ExecutionException {
//        // GIVEN: 초기 데이터 설정
//        Long movieId = 1L;
//        String seatNumber = "A1";
//
//        Seats seat = Seats.builder()
//                .movieId(movieId)
//                .seatNumber(seatNumber)
//                .isReserved(false)
//                .build();
//        seatsRepository.save(seat);
//
//        Movies movies =
//                Movies.builder()
//                .id(movieId)
//                .title("영화1")
//                        .showing(true)
//                        .ageRating("12")
//                        .runningMinutes(120)
//                        .genre(EGenre.ACTION)
//                .build();
//        movieRepository.save(movies);
//
//        ReservationRequest request = ReservationRequest.builder()
//                .movieId(movieId)
//                .seatNumbers(List.of(seatNumber))
//                .userId(100L)
//                .build();
//
//        // 두 개의 스레드가 동시에 예약 시도
//        ExecutorService executorService = Executors.newFixedThreadPool(2);
//
//        Callable<String> task1 = () -> {
//            reservationService.reserveSeats(request);
//            return "Thread 1 Success";
//        };
//
//        Callable<String> task2 = () -> {
//            reservationService.reserveSeats(request);
//            return "Thread 2 Success";
//        };
//
//        // WHEN
//        Future<String> result1 = executorService.submit(task1);
//        Future<String> result2 = executorService.submit(task2);
//
//        executorService.shutdown();
//        executorService.awaitTermination(5, TimeUnit.SECONDS);
//
//        // THEN: 하나의 스레드만 성공해야 함
//        System.out.println(result1.get());
//        System.out.println(result2.get());
//
//        List<Seats> reservedSeats = seatsRepository.findByMovieIdAndSeatNumberIn(movieId, List.of(seatNumber));
//        assertThat(reservedSeats.get(0).getIsReserved()).isTrue();
//    }


    @Test
    void optimistic_lock_test() throws InterruptedException, ExecutionException {
        // GIVEN: 초기 좌석 데이터 설정
        Long movieId = 1L;
        String seatNumber = "A1";

        Seats seat = Seats.builder()
                .movieId(movieId)
                .seatNumber(seatNumber)
                .isReserved(false)
                .build();
        seatsRepository.save(seat);

        Movies movies =
                Movies.builder()
                        .id(movieId)
                        .title("영화1")
                        .showing(true)
                        .ageRating("12")
                        .runningMinutes(120)
                        .genre(EGenre.ACTION)
                        .build();
        movieRepository.save(movies);

        ReservationRequest request = ReservationRequest.builder()
                .movieId(movieId)
                .seatNumbers(List.of(seatNumber))
                .userId(100L)
                .build();
        // 두 스레드가 동시에 예약 시도
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<String> task1 = () -> {
            reservationService.reserveSeats(request);
            return "Thread 1 성공";
        };

        Callable<String> task2 = () -> {
            reservationService.reserveSeats(request);
            return "Thread 2 성공";
        };

        Future<String> result1 = executor.submit(task1);
        Future<String> result2 = executor.submit(task2);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 결과 확인
        System.out.println(result1.get());
        System.out.println(result2.get());

        Seats updatedSeat = seatsRepository.findByMovieIdAndSeatNumberIn(movieId, List.of(seatNumber)).get(0);
        assertThat(updatedSeat.getIsReserved()).isTrue();
    }

    @Test
    void distributed_lock_test() throws InterruptedException {
        Long movieId = 1L;
        String seatNumber = "A1";

        Seats seat = Seats.builder()
                .movieId(movieId)
                .seatNumber(seatNumber)
                .isReserved(false)
                .build();
        seatsRepository.save(seat);

        Movies movies =
                Movies.builder()
                        .id(movieId)
                        .title("영화1")
                        .showing(true)
                        .ageRating("12")
                        .runningMinutes(120)
                        .genre(EGenre.ACTION)
                        .build();
        movieRepository.save(movies);
        Runnable task = () -> {

            ReservationRequest request = ReservationRequest.builder()
                    .movieId(movieId)
                    .seatNumbers(List.of(seatNumber))
                    .userId(100L)
                    .build();
            reservationService.reserveSeats(request);
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

    }
}