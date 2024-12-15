package com.sparta.dto.movie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sparta.domain.movie.Movie;
import com.sparta.domain.movie.MovieRating;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieResponseDto {
    private Integer id;
    private String title;
    private MovieRating rating;
    private LocalDate releaseDate;
    private String thumbnailUrl;
    private Integer duration;
    private String genres;

    @Builder
    private MovieResponseDto(Integer id, String title, MovieRating rating,
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
    public MovieResponseDto() {
    }
}