package com.sparta.multimovieservice.testConfig.repository;

import com.sparta.domain.movie.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestScreeningRepository extends JpaRepository<Screening, Integer> {
}