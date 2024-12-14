package com.sparta.multimovieservice.service;

import com.sparta.multimovieservice.domain.movie.Movie;
import com.sparta.multimovieservice.dto.MovieCreateRequestDto;
import com.sparta.multimovieservice.dto.MovieResponseDto;
import com.sparta.multimovieservice.exception.MovieException;
import com.sparta.multimovieservice.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class MovieService {
    private static final String CACHE_KEY_PREFIX = "movies::search::";
    private static final long CACHE_DURATION_HOURS = 1;

    private final MovieRepository movieRepository;
    private final RedisTemplate<String, List<MovieResponseDto>> redisTemplate;

    public MovieService(MovieRepository movieRepository,
                        RedisTemplate<String, List<MovieResponseDto>> redisTemplate) {
        this.movieRepository = movieRepository;
        this.redisTemplate = redisTemplate;
    }

    public List<MovieResponseDto> getCurrentMovies() {
        String cacheKey = generateCacheKey("all");
        return fetchMoviesFromCacheOrDb(cacheKey, movieRepository::findAllByOrderByReleaseDateDesc);
    }

    @Transactional
    public MovieResponseDto createMovie(MovieCreateRequestDto request) {
        validateMovieRequest(request);
        Movie savedMovie = movieRepository.save(request.toEntity());
        return MovieResponseDto.from(savedMovie);
    }

    public List<MovieResponseDto> searchMovies(String title, String genres) {
        if (title == null && genres == null) {
            return getCurrentMovies();
        }

        String cacheKey = generateCacheKey("title", title, "genres", genres);
        return fetchMoviesFromCacheOrDb(cacheKey, () -> fetchMoviesByCriteria(title, genres));
    }

    private List<MovieResponseDto> fetchMoviesFromCacheOrDb(String cacheKey, FetchMovies fetchMovies) {
        List<MovieResponseDto> cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null && !cachedResult.isEmpty()) {
            log.info("Fetched movies from cache for key: {}", cacheKey);
            return cachedResult;
        }

        List<Movie> movies = fetchMovies.fetch();
        if (movies.isEmpty()) {
            throw new MovieException("Currently there are no movies showing.");
        }

        List<MovieResponseDto> result = movies.stream()
                .map(MovieResponseDto::from)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_DURATION_HOURS, TimeUnit.HOURS);
        log.info("Fetched movies from DB and cached the result for key: {}", cacheKey);

        return result;
    }

    private List<Movie> fetchMoviesByCriteria(String title, String genres) {
        if (title != null && genres != null) {
            return movieRepository.findByTitleContainingAndGenresContaining(title, genres);
        } else if (title != null) {
            return movieRepository.findByTitleContaining(title);
        } else {
            return movieRepository.findByGenresContaining(genres);
        }
    }

    private String generateCacheKey(String... params) {
        StringBuilder keyBuilder = new StringBuilder(CACHE_KEY_PREFIX);
        for (int i = 0; i < params.length; i += 2) {
            String key = params[i];
            String value = (i + 1) < params.length ? params[i + 1] : "null";
            keyBuilder.append(key).append("::").append(value).append("::");
        }
        // Remove trailing "::"
        if (keyBuilder.length() > CACHE_KEY_PREFIX.length()) {
            keyBuilder.setLength(keyBuilder.length() - 2);
        }
        return keyBuilder.toString();
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

    @FunctionalInterface
    private interface FetchMovies {
        List<Movie> fetch();
    }
}