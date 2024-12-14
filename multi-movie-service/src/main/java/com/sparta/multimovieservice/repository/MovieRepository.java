package com.sparta.multimovieservice.repository;

import com.sparta.multimovieservice.domain.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findAllByOrderByReleaseDateDesc();

    List<Movie> findByTitleContainingAndGenresContaining(String title, String genres);

    List<Movie> findByTitleContaining(String title);

    List<Movie> findByGenresContaining(String genres);
}