package com.sparta.multimovieservice.service;

import com.sparta.domain.movie.Movie;
import com.sparta.dto.movie.MovieCreateRequestDto;
import com.sparta.dto.movie.MovieResponseDto;
import com.sparta.exception.MovieException;
import com.sparta.multimovieservice.repository.MovieJpaRepository;
import com.sparta.repository.movie.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovieService {
    private static final String CACHE_KEY_PREFIX = "movies::";
    private static final String SEARCH_CACHE_KEY = CACHE_KEY_PREFIX + "search::";
    private static final String LIST_CACHE_KEY = CACHE_KEY_PREFIX + "list";
    private static final long CACHE_DURATION_HOURS = 1;

    private final MovieRepository movieRepository;
    private final MovieJpaRepository movieJpaRepository;
    private final RedisTemplate<String, List<MovieResponseDto>> redisTemplate;

    public MovieService(MovieRepository movieRepository, MovieJpaRepository movieJpaRepository,
                        RedisTemplate<String, List<MovieResponseDto>> redisTemplate) {
        this.movieRepository = movieRepository;
        this.movieJpaRepository = movieJpaRepository;
        this.redisTemplate = redisTemplate;
    }

    @Cacheable(value = LIST_CACHE_KEY, unless = "#result.isEmpty()")
    public List<MovieResponseDto> getCurrentMovies() {
        log.info("Fetching current movies from database");
        return movieJpaRepository.findAllByOrderByReleaseDateDesc().stream()
                .map(MovieResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {LIST_CACHE_KEY, SEARCH_CACHE_KEY}, allEntries = true)
    public MovieResponseDto createMovie(MovieCreateRequestDto request) {
        validateMovieRequest(request);
        Movie savedMovie = movieRepository.save(request.toEntity());
        clearCache();
        return MovieResponseDto.from(savedMovie);
    }

    @Transactional(readOnly = true)
    public List<MovieResponseDto> searchMovies(String title, String genres) {
        String cacheKey = generateSearchCacheKey(title, genres);

        List<MovieResponseDto> cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null && !cachedResult.isEmpty()) {
            log.info("Cache hit for search with key: {}", cacheKey);
            return cachedResult;
        }

        log.info("Cache miss for search with key: {}", cacheKey);
        List<Movie> movies = fetchMoviesFromDb(title, genres);

        if (movies.isEmpty()) {
            throw new MovieException("Currently there are no movies matching your criteria.");
        }

        List<MovieResponseDto> result = movies.stream()
                .map(MovieResponseDto::from)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_DURATION_HOURS, TimeUnit.HOURS);
        log.info("Cached search results with key: {}", cacheKey);

        return result;
    }

    private List<Movie> fetchMoviesFromDb(String title, String genres) {
        List<Movie> movies;
        try {
            if (title == null && genres == null) {
                movies = movieJpaRepository.findAllByOrderByReleaseDateDesc();
            } else if (title != null && genres != null) {
                log.info("Searching by title: '{}' and genres: '{}'", title, genres);
                movies = movieJpaRepository.findByTitleAndGenresContaining(title, genres);
            } else if (title != null) {
                log.info("Searching by title: '{}'", title);
                movies = movieJpaRepository.findByTitleContaining(title);
            } else {
                log.info("Searching by genres: '{}'", genres);
                movies = movieJpaRepository.findByGenresContaining(genres);
            }
            log.info("Found {} movies in database", movies.size());
            return movies;
        } catch (Exception e) {
            log.error("Error during database search: ", e);
            throw new MovieException("Error occurred while searching movies: " + e.getMessage());
        }
    }

    private String generateSearchCacheKey(String title, String genres) {
        StringBuilder keyBuilder = new StringBuilder(SEARCH_CACHE_KEY);
        if (title != null) {
            keyBuilder.append("title::").append(title.toLowerCase()).append("::");
        }
        if (genres != null) {
            keyBuilder.append("genres::").append(genres.toLowerCase()).append("::");
        }
        return keyBuilder.toString();
    }

    private void clearCache() {
        try {
            redisTemplate.delete(LIST_CACHE_KEY);
            redisTemplate.keys(SEARCH_CACHE_KEY + "*").forEach(key -> redisTemplate.delete(key));
            log.info("Cache cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing cache", e);
        }
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
            throw new MovieException(fieldName + " is required.");
        }
    }
}