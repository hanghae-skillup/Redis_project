package com.example.api.controller;

import com.example.common.dto.MessageResponse;
import com.example.common.dto.MovieCreateRequest;
import com.example.common.dto.MovieDto;

import com.example.service.service.MovieService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<MovieDto> getMovieById(@PathVariable Long movieId) {
        MovieDto movieById = movieService.getMovieById(movieId);
        return ResponseEntity.ok(movieById);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieDto>> searchMovies(@RequestParam(name = "title", required = false) String title,
                                       @RequestParam(name = "genre", required = false) String genre) {
        List<MovieDto> movieDtos = movieService.searchMovies(title, genre);
        return ResponseEntity.ok(movieDtos);
    }

    @PatchMapping("/{movieId}/showing")
    public ResponseEntity<MessageResponse> updateShowing(@PathVariable Long movieId, @RequestParam boolean showing, @RequestParam Long userId) {
        movieService.updateShowing(movieId, showing, userId);
        return ResponseEntity.ok(new MessageResponse("영화 상영 여부가 변경되었습니다."));
    }

    @PostMapping("")
    public ResponseEntity<MessageResponse>  createMovie(@RequestBody MovieCreateRequest movieCreateRequest) {
        movieService.createMovie(movieCreateRequest);
        return ResponseEntity.ok(new MessageResponse("영화가 등록되었습니다."));
    }
}