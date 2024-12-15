package com.example.service.service;

import com.example.common.dto.MovieCreateRequest;
import com.example.common.dto.MovieDto;
import com.example.common.enums.EGenre;
import com.example.domain.entity.Movies;
import com.example.domain.repository.MovieRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<MovieDto> getShowingMovieDesc() {
        return movieRepository.findByShowingOrderByReleaseDateDesc(true)
                .stream()
                .map(Movies::toDto)
                .collect(Collectors.toList());
    }


    public MovieDto getMovieById(Long movieId) {
        Movies movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found with id: " + movieId));
        return movie.toDto();
    }

    public MovieDto updateShowing(Long movieId, boolean showing, Long userId) {
        Movies movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found with id: " + movieId));
        movie.updateShowing(showing, userId);
        return movie.toDto();
    }


    @Transactional
    public void createMovie(MovieCreateRequest movieCreateRequest) {
        Movies movie = Movies.createMovieBy(movieCreateRequest);
        movieRepository.save(movie);
    }

    @Cacheable(value = "searchMoviesCache",
            keyGenerator = "customKeyGenerator",
            unless = "#result.size() == 0")
    public List<MovieDto> searchMovies(String title, String genre) {
        Boolean isNotEmptyGenre = genre != null && !genre.isEmpty();
        EGenre genreEnum = isNotEmptyGenre? EGenre.valueOf(genre):null;
        return movieRepository.searchMovies(title, genreEnum);
    }
}
