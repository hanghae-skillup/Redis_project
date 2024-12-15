package com.sparta.multimovieservice.testConfig.repository;

import com.sparta.domain.theater.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestTheaterRepository extends JpaRepository<Theater, Integer> {
}