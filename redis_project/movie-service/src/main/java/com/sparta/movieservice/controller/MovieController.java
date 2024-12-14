package com.sparta.movieservice.controller;

import com.sparta.movieservice.dto.MovieCreateRequestDto;
import com.sparta.movieservice.dto.MovieResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sparta.movieservice.service.MovieService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieResponseDto>> getCurrentMovies() {
        return ResponseEntity.ok(movieService.getCurrentMovies());
    }

    @PostMapping
    public ResponseEntity<MovieResponseDto> createMovie(@RequestBody @Valid MovieCreateRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movieService.createMovie(request));
    }
}