package com.example.domain.repository;

import com.example.domain.entity.QSeats;
import com.example.domain.entity.Seats;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.domain.entity.QSeats.seats;
import static com.example.domain.entity.QReservations.reservations;

@Repository
@RequiredArgsConstructor
public class CustomSeatRepositoryImpl implements CustomSeatRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Seats> findSeatsByMovieIdAndUserId(Long movieId, Long userId) {
        return queryFactory
                .selectFrom(seats)
                .where(seats.reservationId.in(
                        JPAExpressions.select(reservations.id) // 서브쿼리: reservation_id에 해당
                                .from(reservations)
                                .where(reservations.userId.eq(userId) // user_id 조건
                                        .and(reservations.movieId.eq(movieId))) // movie_id 조건
                ))
                .fetch();
    }


    @Override
    public List<Seats> findSeatsWithLock(Long movieId, List<String> seatNumbers) {
        QSeats seats = QSeats.seats;

        return queryFactory
                .selectFrom(seats)
                .where(seats.movieId.eq(movieId)
                        .and(seats.seatNumber.in(seatNumbers)))
                .setHint("javax.persistence.lock.timeout", 0) // 락 타임아웃 설정 (0은 즉시 실패)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE) // 비관적 락 설정
                .fetch();
    }
}