package com.sparta.multibookingservice.repository;

import com.sparta.domain.booking.Booking;
import com.sparta.domain.movie.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;

public interface BookingJpaRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b JOIN FETCH b.seat WHERE b.screening = :screening")
    List<Booking> findByScreening(@Param("screening") Screening screening);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.screening.id = :screeningId AND b.userId = :userId")
    int countByScreeningIdAndUserId(@Param("screeningId") Integer screeningId, @Param("userId") String userId);

    // Pessimistic Lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.screening = :screening")
    List<Booking> findByScreeningWithPessimisticLock(@Param("screening") Screening screening);
}