package com.example.service.service;

import com.example.common.dto.ReservationRequest;
import com.example.common.lock.DistributedLock;
import com.example.common.lock.DistributedLockExecutor;
import com.example.domain.entity.Movies;
import com.example.domain.entity.Reservations;
import com.example.domain.entity.Seats;
import com.example.domain.repository.MovieRepository;
import com.example.domain.repository.ReservationRepository;
import com.example.domain.repository.SeatRepository;
import com.example.service.event.ReservationCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    private final ApplicationEventPublisher eventPublisher;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final MovieRepository movieRepository;
    private final DistributedLockExecutor lockExecutor;

    @Transactional
//    @DistributedLock(key = "reserveSeatsLock", leaseTime = 10)
    public void reserveSeats(ReservationRequest request) {
        String lockKey = "reserveSeatsLock:" + request.getMovieId(); // Redis 락 키
        lockExecutor.executeWithLock(lockKey, 10, () -> {

            // 1. 영화가 존재하는지 확인
        Movies movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("영화가 존재하지 않습니다."));


        if(movie.notShowing()){
            throw new IllegalArgumentException("상영중인 영화가 아닙니다.");
        }

        // 2. 영화에 좌석이 없으면 좌석 자동 생성
        createSeatsIfNotExists(movie);

        // 3. 좌석 정보 가져오기
        List<Seats> reservingSeats = seatRepository.findByMovieIdAndSeatNumberIn(
                request.getMovieId(), request.getSeatNumbers()
        );

        //  pessimistic lock 적용
//        List<Seats> reservingSeats = seatRepository.findSeatsWithLock(
//                request.getMovieId(), request.getSeatNumbers()
//        );


        // 4. 좌석이 존재하는지 확인
        if (reservingSeats.size() != request.getSeatNumbers().size() || reservingSeats.isEmpty()) {
            throw new IllegalArgumentException("좌석이 존재하지 않습니다.");
        }

        // 같은 유저가 예약한 좌석이 있는지 확인
        List<Seats> reservedSeats = seatRepository.findSeatsByMovieIdAndUserId(
                request.getMovieId(), request.getUserId()
        );
        int totalReservedSeats = reservedSeats.size() + reservingSeats.size();
        if(totalReservedSeats > 5){
            throw new IllegalArgumentException("예매 가능한 좌석은 5개 입니다.");
        }

        for (Seats seat : reservingSeats) {
            if (Boolean.TRUE.equals(seat.getIsReserved())) {
                throw new IllegalArgumentException("이미 예약된 좌석이 포함되어 있습니다.");
            }
        }


        // 4. 연속된 좌석인지 검증, reserveSeats 포함
        List<Seats> allSeats = new ArrayList<>(reservingSeats);
        allSeats.addAll(reservedSeats);
        validateContinuousSeats(allSeats);

        // 5. 좌석 예약 가능 여부 확인

        // 6. 예약 저장 및 좌석 상태 업데이트
        seatRepository.saveAll(reservingSeats);

        // 7. 예약 정보 저장
        Reservations reservation = Reservations.builder()
                .userId(request.getUserId())
                .movieId(request.getMovieId())
                .reservedDate(LocalDate.now())
                .build();
        reservation.createBy(request.getUserId());
        Reservations saved = reservationRepository.save(reservation);
        for(Seats seat : reservingSeats){
            seat.updateReservation(request.getUserId(), reservation.getId());
        }

        List<String> reservedSeatNumbers =
                reservingSeats.stream().map(Seats::getSeatNumber).toList();

        // 예약 완료 이벤트 발행
        eventPublisher.publishEvent(new ReservationCompletedEvent(
                this, saved.getId(), request.getUserId(),
                request.getMovieId(), reservedSeatNumbers
        ));

            return null;
        });
    }

    private void createSeatsIfNotExists(Movies movie) {
        // 영화에 좌석이 존재하는지 확인
        if (seatRepository.existsByMovieId(movie.getId())) {
            return;
        }

        // 기본 좌석 생성 (5x5 형태)
        List<Seats> seats = new ArrayList<>();
        for (char row = 'A'; row <= 'E'; row++) { // 5행
            for (int col = 1; col <= 5; col++) { // 5열
                Seats seat = Seats.builder()
                        .movieId(movie.getId())
                        .seatNumber(row + String.valueOf(col)) // A1, A2, ...
                        .isReserved(false) // 기본값: 예약되지 않음
                        .build();
                seat.updateBy(1L); // 관리자로 생성
                seat.createBy(1L); // 관리자로 생성
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
    }


    private void validateContinuousSeats(List<Seats> seats) {
        // 좌석 번호가 연속적이어야 함을 검증
        // 예: A1, A2, A3 형태로 되어 있는지 확인
        if (seats.size() <= 1) {
            return;
        }
        seats.sort(Comparator.comparing(Seats::getSeatNumber)); // 좌석 정렬
        for (int i = 1; i < seats.size(); i++) {
            String prevSeat = seats.get(i - 1).getSeatNumber();
            String currentSeat = seats.get(i).getSeatNumber();
            if (!isConsecutive(prevSeat, currentSeat)) {
                throw new IllegalArgumentException("좌석은 연속된 형태로만 예약 가능합니다.");
            }
        }
    }

    private boolean isConsecutive(String prevSeat, String currentSeat) {
        // 좌석 번호 연속성 확인 (예: A1 -> A2)
        char prevRow = prevSeat.charAt(0);
        char currentRow = currentSeat.charAt(0);
        int prevCol = Integer.parseInt(prevSeat.substring(1));
        int currentCol = Integer.parseInt(currentSeat.substring(1));
        return prevRow == currentRow && currentCol == prevCol + 1;
    }
}
