
package com.example.infrastructure.repository;

import com.example.domain.entity.Reservations;
import com.example.domain.repository.ReservationRepository;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface JpaReservationRepository extends JpaRepository<Reservations, Long>, ReservationRepository {
    @Override
    List<Reservations> findByUserId(Long userId);

    @Override
    List<Reservations> findByMovieId(Long movieId);
}