package com.sparta.multibookingservice.repository;

import com.sparta.domain.movie.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ScreeningJpaRepository extends JpaRepository<Screening, Long> {
    @Query("SELECT s FROM Screening s " +
            "JOIN FETCH s.movie " +
            "JOIN FETCH s.theater " +
            "WHERE s.id = :id")
    Optional<Screening> findByIdWithTheaterAndMovie(@Param("id") Integer id);
}
