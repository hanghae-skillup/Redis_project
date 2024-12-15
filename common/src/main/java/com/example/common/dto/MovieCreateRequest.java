package com.example.common.dto;


import lombok.Getter;

@Getter
public class MovieCreateRequest {
    private String title;
    private String ageRating;
    private String genre;
    private String thumbNailUrl;
    private Integer runningMinutes;
    private Boolean isShowing;
    private String releaseDate;
    private Long userId;
}
