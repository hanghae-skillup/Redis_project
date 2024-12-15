package com.example.domain.repository;

import com.example.common.dto.MovieDto;
import com.example.common.enums.EGenre;
import com.example.domain.entity.Movies;

import java.util.List;

public interface CustomMovieRepository {
    List<MovieDto> searchMovies(String title, EGenre genre);
}