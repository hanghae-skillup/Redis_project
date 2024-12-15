package com.sparta.multimovieservice.testConfig.repository;

import com.sparta.domain.movie.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestSeatRepository extends JpaRepository<Seat, Integer> {
}