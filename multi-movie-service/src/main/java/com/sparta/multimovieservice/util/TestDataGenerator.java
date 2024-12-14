package com.sparta.multimovieservice.util;

import com.sparta.multimovieservice.domain.movie.Movie;
import com.sparta.multimovieservice.domain.movie.MovieRating;
import com.sparta.multimovieservice.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@Slf4j
public class TestDataGenerator {

    private final MovieRepository movieRepository;

    private static final String[] GENRES = {
            "액션", "드라마", "코미디", "로맨스", "공포",
            "SF", "애니메이션", "다큐멘터리", "스릴러", "판타지"
    };

    private static final String[] TITLE_PREFIXES = {
            "위대한", "비밀의", "마지막", "새로운", "신비한",
            "황금", "빛나는", "영원한", "달콤한", "차가운"
    };

    private static final String[] TITLE_SUFFIXES = {
            "여행", "전쟁", "사랑", "모험", "세계",
            "운명", "약속", "기적", "희망", "꿈"
    };

    public TestDataGenerator(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional
    public void generateTestData() {

        movieRepository.deleteAll();

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
        String title = generateRandomTitle();
        String rating = generateRandomRating();
        int duration = generateRandomDuration();
        String thumbnailUrl = generateThumbnailUrl();

        return Movie.builder()
                .title(title)
                .rating(MovieRating.valueOf(rating))
                .releaseDate(releaseDate)
                .thumbnailUrl(thumbnailUrl)
                .duration(duration)
                .genres(genre)
                .build();
    }

    private String generateRandomTitle() {
        String prefix = TITLE_PREFIXES[new Random().nextInt(TITLE_PREFIXES.length)];
        String suffix = TITLE_SUFFIXES[new Random().nextInt(TITLE_SUFFIXES.length)];
        return prefix + " " + suffix + " " + UUID.randomUUID().toString().substring(0, 4);
    }

    private String generateRandomRating() {
        String[] ratings = {"ALL", "TWELVE", "FIFTEEN", "ADULT"};
        return ratings[new Random().nextInt(ratings.length)];
    }

    private int generateRandomDuration() {
        return 60 + new Random().nextInt(121);
    }

    private String generateThumbnailUrl() {
        return "https://example.com/movies/thumbnail/" + UUID.randomUUID().toString();
    }
}