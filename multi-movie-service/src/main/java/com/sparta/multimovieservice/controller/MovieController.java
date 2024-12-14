package com.sparta.multimovieservice.controller;

import com.sparta.multimovieservice.dto.MovieCreateRequestDto;
import com.sparta.multimovieservice.dto.MovieResponseDto;
import com.sparta.multimovieservice.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Slf4j
public class MovieController {
    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieResponseDto>> getCurrentMovies() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return ResponseEntity.ok()
                .header("X-Memory-Used", String.valueOf(usedMemory / (1024 * 1024)))
                .header("X-Memory-Total", String.valueOf(totalMemory / (1024 * 1024)))
                .header("X-Memory-Rss", String.valueOf(runtime.maxMemory() / (1024 * 1024)))
                .body(movieService.getCurrentMovies());
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponseDto>> searchMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genres) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        log.info("Received request to search movies with title: {} and genres: {}", title, genres);
        List<MovieResponseDto> movies = movieService.searchMovies(title, genres);
        return ResponseEntity.ok()
                .header("X-Memory-Used", String.valueOf(usedMemory / (1024 * 1024)))
                .header("X-Memory-Total", String.valueOf(totalMemory / (1024 * 1024)))
                .header("X-Memory-Rss", String.valueOf(runtime.maxMemory() / (1024 * 1024)))
                .body(movies);
    }

    @PostMapping
    public ResponseEntity<MovieResponseDto> createMovie(@RequestBody @Valid MovieCreateRequestDto request) {
        log.info("Received request to create movie: {}", request.getTitle());
        MovieResponseDto createdMovie = movieService.createMovie(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdMovie);
    }
}
