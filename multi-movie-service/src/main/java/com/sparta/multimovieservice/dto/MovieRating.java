package com.sparta.multimovieservice.dto;
import lombok.Getter;

@Getter
public enum MovieRating {
    ALL("전체 관람가"),
    TWELVE("12세 이상 관람가"),
    FIFTEEN("15세 이상 관람가"),
    ADULT("청소년 관람불가");

    private final String description;

    MovieRating(String description) {
        this.description = description;
    }
}