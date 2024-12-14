package com.sparta.movieservice.domain.movie;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieRating rating;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @Column(nullable = false)
    private String genres;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "movie")
    private List<Screening> screenings = new ArrayList<>();

    @Builder
    public Movie(String title, MovieRating rating, Integer duration, LocalDate releaseDate,
                 String thumbnailUrl, String genres) {
        this.title = title;
        this.rating = rating;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.thumbnailUrl = thumbnailUrl;
        this.genres = genres;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
