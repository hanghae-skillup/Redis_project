package com.example.domain.repository;

import com.example.domain.entity.Reservations;

import java.util.List;

public interface ReservationRepository {
    Reservations save(Reservations reservation);
    List<Reservations> findByUserId(Long userId);
    List<Reservations> findByMovieId(Long movieId);
}
