package com.sparta.movieservice.dto;

import com.sparta.movieservice.domain.movie.Movie;
import com.sparta.movieservice.domain.movie.MovieRating;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MovieResponseDto {
    private Long id;
    private String title;
    private MovieRating rating;
    private LocalDate releaseDate;
    private String thumbnailUrl;
    private Integer duration;
    private String genres;

    @Builder
    private MovieResponseDto(Long id, String title, MovieRating rating,
                             LocalDate releaseDate, String thumbnailUrl,
                             Integer duration, String genres) {
        this.id = id;
        this.title = title;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.thumbnailUrl = thumbnailUrl;
        this.duration = duration;
        this.genres = genres;
    }

    public static MovieResponseDto from(Movie movie) {
        return MovieResponseDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .rating(movie.getRating())
                .releaseDate(movie.getReleaseDate())
                .thumbnailUrl(movie.getThumbnailUrl())
                .duration(movie.getDuration())
                .genres(movie.getGenres())
                .build();
    }
}