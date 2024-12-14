package com.sparta.movieservice.repository;

import com.sparta.movieservice.domain.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findAllByOrderByReleaseDateDesc();
}