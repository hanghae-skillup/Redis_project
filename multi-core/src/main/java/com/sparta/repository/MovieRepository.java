package com.sparta.repository;

import com.sparta.domain.movie.Movie;
import java.util.List;
import java.util.Optional;

public interface MovieRepository {
    List<Movie> findAll();
    Optional<Movie> findById(Long id);
    Movie save(Movie movie);
    void delete(Movie movie);

    List<Movie> findAllByOrderByReleaseDateDesc();
    List<Movie> findByTitleContaining(String title);
    List<Movie> findByGenresContaining(String genres);
    List<Movie> findByTitleAndGenresContaining(String title, String genres);

}