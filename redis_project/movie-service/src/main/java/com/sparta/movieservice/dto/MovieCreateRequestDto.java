package com.sparta.movieservice.dto;

import com.sparta.movieservice.domain.movie.Movie;
import com.sparta.movieservice.domain.movie.MovieRating;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MovieCreateRequestDto {
    @NotBlank
    private String title;

    @NotNull
    private MovieRating rating;

    @NotNull
    private LocalDate releaseDate;

    @NotBlank
    private String thumbnailUrl;

    @NotNull
    @Min(1)
    private Integer duration;

    @NotEmpty
    private String genres;


    public Movie toEntity() {
        return Movie.builder()
                .title(title)
                .rating(rating)
                .releaseDate(releaseDate)
                .thumbnailUrl(thumbnailUrl)
                .duration(duration)
                .genres(genres)
                .build();
    }
}