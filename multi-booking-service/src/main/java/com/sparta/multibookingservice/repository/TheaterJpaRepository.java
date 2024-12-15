package com.sparta.multibookingservice.repository;

import com.sparta.domain.theater.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TheaterJpaRepository extends JpaRepository<Theater, Integer> {

    @Query("SELECT DISTINCT t FROM Theater t LEFT JOIN FETCH t.seats WHERE t.id = :id")
    Optional<Theater> findByIdWithSeats(@Param("id") Integer id);

    @Query("SELECT DISTINCT t FROM Theater t LEFT JOIN FETCH t.seats")
    List<Theater> findAllWithSeats();
}