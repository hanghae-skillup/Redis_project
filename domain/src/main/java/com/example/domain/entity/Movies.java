package com.example.domain.entity;


import com.example.common.dto.MovieCreateRequest;
import com.example.common.dto.MovieDto;
import com.example.common.dto.MovieUpdateRequest;
import com.example.common.enums.EGenre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity @Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Movies extends Timestamped{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long id;

    private String title;
    private String ageRating;

    @Enumerated(EnumType.STRING)
    private EGenre genre;

    private String thumbnailUrl;
    private Integer runningMinutes;
    private Boolean showing; // 상영중인지 여부
    private LocalDate releaseDate;

    public void update(MovieUpdateRequest movieUpdateRequest){
        this.title = movieUpdateRequest.getTitle();
        this.ageRating = movieUpdateRequest.getAgeRating();
        this.genre = EGenre.valueOf(movieUpdateRequest.getGenre());
        this.thumbnailUrl = movieUpdateRequest.getThumbNailUrl();
        this.runningMinutes = movieUpdateRequest.getRunningMinutes();
        this.showing = movieUpdateRequest.getIsShowing();
        this.updateBy(movieUpdateRequest.getUserId());
    }

    public static Movies createMovieBy(MovieCreateRequest movieCreateRequest){
        Movies movie =  Movies.builder()
                .title(movieCreateRequest.getTitle())
                .ageRating(movieCreateRequest.getAgeRating())
                .genre(EGenre.valueOf(movieCreateRequest.getGenre()))
                .thumbnailUrl(movieCreateRequest.getThumbNailUrl())
                .runningMinutes(movieCreateRequest.getRunningMinutes())
                .showing(movieCreateRequest.getIsShowing())
                .releaseDate(LocalDate.parse(movieCreateRequest.getReleaseDate()))
                .build();
        movie.createBy(movieCreateRequest.getUserId());
        return movie;
    }

    public MovieDto toDto() {
        return MovieDto.builder()
                .id(this.id)
                .title(this.title)
                .genre(this.genre.name())
                .thumbNailUrl(this.thumbnailUrl)
                .runningMinutes(this.runningMinutes)
                .showing(this.showing)
                .releaseDate(this.releaseDate.toString())
                .build();
    }

    public void updateShowing(Boolean showing, Long userId){
        this.showing = showing;
        this.updateBy(userId);
    }
}
