package com.sparta.multimovieservice.util;

import com.sparta.domain.movie.Movie;
import com.sparta.domain.movie.MovieRating;
import com.sparta.multimovieservice.impl.MovieRepositoryImpl;
import com.sparta.repository.MovieRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TestDataGenerator {
    private final MovieRepositoryImpl movieRepository;

    private static final String[] GENRES = {
            "Action", "Drama", "Comedy", "Romance", "Horror",
            "SF", "Animation", "Documentary", "Thriller", "Fantasy"
    };

    private static final String[] TITLE_PREFIXES = {
            "Great", "Secret", "Last", "New", "Mysterious",
            "Golden", "Shining", "Eternal", "Sweet", "Cold"
    };

    private static final String[] TITLE_SUFFIXES = {
            "Journey", "War", "Love", "Adventure", "World",
            "Destiny", "Promise", "Miracle", "Hope", "Dream"
    };

    @Transactional
    public void generateTestData() {
        List<Movie> movies = new ArrayList<>();
        LocalDate baseDate = LocalDate.now().minusYears(2);

        for (String genre : GENRES) {
            for (int i = 0; i < 50; i++) {
                Movie movie = createMovie(genre, baseDate.plusDays(i));
                movies.add(movie);
            }
        }

        movieRepository.saveAll(movies);
        log.info("Generated {} test movies", movies.size());
    }

    private Movie createMovie(String genre, LocalDate releaseDate) {
        Random random = new Random();
        String title = generateRandomTitle();
        MovieRating rating = MovieRating.values()[random.nextInt(MovieRating.values().length)];
        int duration = 60 + random.nextInt(121);
        String thumbnailUrl = "https://example.com/movies/thumbnail/" + UUID.randomUUID();

        return Movie.builder()
                .title(title)
                .rating(rating)
                .releaseDate(releaseDate)
                .thumbnailUrl(thumbnailUrl)
                .duration(duration)
                .genres(genre)
                .build();
    }

    private String generateRandomTitle() {
        Random random = new Random();
        String prefix = TITLE_PREFIXES[random.nextInt(TITLE_PREFIXES.length)];
        String suffix = TITLE_SUFFIXES[random.nextInt(TITLE_SUFFIXES.length)];
        return prefix + " " + suffix + " " + UUID.randomUUID().toString().substring(0, 4);
    }
}