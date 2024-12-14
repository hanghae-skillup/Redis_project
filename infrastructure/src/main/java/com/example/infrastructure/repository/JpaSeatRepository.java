
package com.example.infrastructure.repository;

import com.example.domain.entity.Seats;
import com.example.domain.repository.SeatRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaSeatRepository extends JpaRepository<Seats, Long>, SeatRepository {
    @Override
    List<Seats> findByMovieId(Long movieId);
}
