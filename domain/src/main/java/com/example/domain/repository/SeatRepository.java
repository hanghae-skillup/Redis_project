package com.example.domain.repository;

import com.example.domain.entity.Seats;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seats, Long>, CustomSeatRepository {
    List<Seats> findByMovieIdAndSeatNumberIn(Long movieId, List<String> seatNumbers);
    List<Seats> findByMovieId(Long movieId);
    boolean existsByMovieId(Long movieId);


}