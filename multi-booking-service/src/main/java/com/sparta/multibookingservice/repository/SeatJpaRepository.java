package com.sparta.multibookingservice.repository;

import com.sparta.domain.movie.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatJpaRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findByTheaterIdAndSeatRowAndSeatColumn(Integer theaterId, String row, int column);

    @Query("SELECT s FROM Seat s WHERE s.theater.id = :theaterId AND s.seatRow = :row ORDER BY s.seatColumn")
    List<Seat> findByTheaterIdAndSeatRow(@Param("theaterId") Integer theaterId, @Param("row") String row);

    // Optimistic Lock
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM Seat s WHERE s.theater.id = :theaterId AND s.seatRow = :row AND s.seatColumn = :column")
    Optional<Seat> findByTheaterIdAndSeatRowAndSeatColumnWithLock(
            @Param("theaterId") Integer theaterId,
            @Param("row") String row,
            @Param("column") Integer column
    );
}