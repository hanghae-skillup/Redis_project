package com.sparta.dto.movie;

import com.sparta.domain.movie.Movie;
import com.sparta.domain.movie.MovieRating;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MovieCreateRequestDto {
    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotNull(message = "Rating is mandatory")
    private MovieRating rating;

    @NotNull(message = "Release date is mandatory")
    private LocalDate releaseDate;

    @NotBlank(message = "Thumbnail URL is mandatory")
    private String thumbnailUrl;

    @NotNull(message = "Duration is mandatory")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    @NotEmpty(message = "Genres are mandatory")
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