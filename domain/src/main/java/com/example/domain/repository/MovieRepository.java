package com.example.domain.repository;


import com.example.domain.entity.Movies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MovieRepository extends JpaRepository<Movies, Long>, CustomMovieRepository {
    List<Movies> findByGenre(String genre);
    List<Movies> findByShowingOrderByReleaseDateDesc(Boolean showing);
    List<Movies> findByIdAndShowing(Long id, Boolean showing);
}