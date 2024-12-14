package com.example.domain.repository;


import com.example.domain.entity.Movies;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface MovieRepository {
    Movies save(Movies movie);
    void deleteById(Long id);
    List<Movies> findAll();
    List<Movies> findByGenre(String genre);
    List<Movies> findByShowingOrderByReleaseDateDesc(Boolean showing);
    Optional<Movies> findById(Long id);
}