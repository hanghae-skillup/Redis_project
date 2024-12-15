package com.sparta.multimovieservice.impl;

import com.sparta.domain.movie.Movie;
import com.sparta.repository.MovieRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepositoryImpl extends JpaRepository<Movie, Long>, MovieRepository {
    @Override
    @Query("SELECT m FROM Movie m ORDER BY m.releaseDate DESC")
    List<Movie> findAllByOrderByReleaseDateDesc();

    @Override
    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY m.releaseDate DESC")
    List<Movie> findByTitleContaining(@Param("title") String title);

    @Override
    @Query("SELECT m FROM Movie m WHERE LOWER(m.genres) LIKE LOWER(CONCAT('%', :genres, '%')) ORDER BY m.releaseDate DESC")
    List<Movie> findByGenresContaining(@Param("genres") String genres);

    @Override
    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%')) AND LOWER(m.genres) LIKE LOWER(CONCAT('%', :genres, '%')) ORDER BY m.releaseDate DESC")
    List<Movie> findByTitleAndGenresContaining(@Param("title") String title, @Param("genres") String genres);

}
