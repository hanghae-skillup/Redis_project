package com.example.domain.repository;

import com.example.domain.entity.Seats;

import java.util.List;

public interface CustomSeatRepository {
    List<Seats> findSeatsByMovieIdAndUserId(Long movieId, Long userId);
}
