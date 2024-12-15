package com.example.common.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Builder
@Getter @ToString
public class MovieDto implements Serializable {

    private static final long serialVersionUID = 1L; // 직렬화 버전 관리용 ID

    private final Long id;
    private final String title;
    private final String genre;
    private final String thumbNailUrl;
    private final Integer runningMinutes;
    private final Boolean showing;
    private final String releaseDate;

    @JsonCreator
    public MovieDto(
            @JsonProperty("id") Long id,
            @JsonProperty("title") String title,
            @JsonProperty("genre") String genre,
            @JsonProperty("thumbNailUrl") String thumbNailUrl,
            @JsonProperty("runningMinutes") Integer runningMinutes,
            @JsonProperty("showing") Boolean showing,
            @JsonProperty("releaseDate") String releaseDate) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.thumbNailUrl = thumbNailUrl;
        this.runningMinutes = runningMinutes;
        this.showing = showing;
        this.releaseDate = releaseDate;
    }
}
