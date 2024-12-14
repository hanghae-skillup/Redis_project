package com.example.common.dto;


import lombok.*;

@Builder @RequiredArgsConstructor
@Getter @ToString
public class MovieDto {
    private final Long id;
    private final String title;
    private final String genre;
    private final String thumbNailUrl;
    private final Integer runningMinutes;
    private final Boolean showing;
    private final String releaseDate;
}
