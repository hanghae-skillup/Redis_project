package com.example.domain.repository;

import com.example.domain.entity.Reservations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservations, Long> {
    List<Reservations> findByUserIdAndMovieId(Long userId, Long movieId);
}