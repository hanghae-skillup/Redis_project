package com.example.domain.repository;

import com.example.domain.entity.Seats;

import java.util.List;

public interface SeatRepository {
    List<Seats> findByMovieId(Long movieId);
}
