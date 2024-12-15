package com.example.api.controller;

import com.example.common.dto.MovieCreateRequest;
import com.example.common.dto.MovieDto;

import com.example.service.service.MovieService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // 모든 영화 조회
    @GetMapping
    public List<MovieDto> getAllMovies() {
        return movieService.getShowingMovieDesc();
    }

    // 특정 영화 조회
    @GetMapping("/{movieId}")
    public MovieDto getMovieById(@PathVariable Long movieId) {
        return movieService.getMovieById(movieId);
    }

    @GetMapping("/search")
    public List<MovieDto> searchMovies(@RequestParam(name = "title", required = false) String title,
                                       @RequestParam(name = "genre", required = false) String genre) {
        return movieService.searchMovies(title, genre);
    }

    @PatchMapping("/{movieId}/showing")
    public void updateShowing(@PathVariable Long movieId, @RequestParam boolean showing, @RequestParam Long userId) {
        movieService.updateShowing(movieId, showing, userId);
    }

    @PostMapping("")
    public void createMovie(@RequestBody MovieCreateRequest movieCreateRequest) {
        movieService.createMovie(movieCreateRequest);
    }
}