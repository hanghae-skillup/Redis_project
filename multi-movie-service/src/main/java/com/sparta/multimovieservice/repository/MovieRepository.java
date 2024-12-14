package com.sparta.multimovieservice.repository;

import com.sparta.multimovieservice.domain.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    // 기존 메서드
    List<Movie> findAllByOrderByReleaseDateDesc();

    // LIKE 검색을 명시적으로 지정하고, 대소문자 구분 없이 검색
    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY m.releaseDate DESC")
    List<Movie> findByTitleContainingOrderByReleaseDateDesc(@Param("title") String title);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.genres) LIKE LOWER(CONCAT('%', :genres, '%')) ORDER BY m.releaseDate DESC")
    List<Movie> findByGenresContainingOrderByReleaseDateDesc(@Param("genres") String genres);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%')) AND LOWER(m.genres) LIKE LOWER(CONCAT('%', :genres, '%')) ORDER BY m.releaseDate DESC")
    List<Movie> findByTitleContainingAndGenresContainingOrderByReleaseDateDesc(
            @Param("title") String title,
            @Param("genres") String genres
    );
}