package com.example.domain.repository;

import com.example.domain.entity.Seats;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
}