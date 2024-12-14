package com.sparta.movieservice.service;

import com.sparta.movieservice.domain.movie.Movie;
import com.sparta.movieservice.dto.MovieCreateRequestDto;
import com.sparta.movieservice.dto.MovieResponseDto;
import com.sparta.movieservice.exception.MovieException;
import com.sparta.movieservice.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
public class MovieService {
    private static final String NO_MOVIES_MESSAGE = "Currently there are no movies showing.";

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<MovieResponseDto> getCurrentMovies() {
        List<Movie> movies = movieRepository.findAllByOrderByReleaseDateDesc();
        if (movies.isEmpty()) {
            throw new MovieException(NO_MOVIES_MESSAGE);
        }
        return movies.stream()
                .map(MovieResponseDto::from)
                .toList();
    }

    @Transactional
    public MovieResponseDto createMovie(MovieCreateRequestDto request) {
        validateMovieRequest(request);
        Movie savedMovie = movieRepository.save(request.toEntity());
        return MovieResponseDto.from(savedMovie);
    }

    private void validateMovieRequest(MovieCreateRequestDto request) {
        validateNotNull(request.getTitle(), "title");
        validateNotNull(request.getRating(), "rating");
        validateNotNull(request.getReleaseDate(), "releaseDate");
        validateNotNull(request.getThumbnailUrl(), "url");
        validateNotNull(request.getDuration(), "duration");
        validateNotNull(request.getGenres(), "genres");
    }

    private void validateNotNull(Object value, String fieldName) {
        if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
            throw new MovieException(fieldName + "is required.");
        }
    }
}