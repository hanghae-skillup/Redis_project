package com.sparta.multibookingservice.config;

import com.sparta.domain.movie.Movie;
import com.sparta.repository.movie.MovieRepository;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
public class MovieRepositoryConfig {
    @Bean
    public MovieRepository movieRepository(EntityManager entityManager) {
        return new MovieRepository() {
            @Override
            public List<Movie> findAll() {
                return List.of();
            }

            @Override
            public Optional<Movie> findById(Long id) {
                return Optional.empty();
            }

            @Override
            public Movie save(Movie movie) {
                return null;
            }

            @Override
            public void delete(Movie movie) {

            }

            @Override
            public List<Movie> findAllByOrderByReleaseDateDesc() {
                return List.of();
            }

            @Override
            public List<Movie> findByTitleContaining(String title) {
                return List.of();
            }

            @Override
            public List<Movie> findByGenresContaining(String genres) {
                return List.of();
            }

            @Override
            public List<Movie> findByTitleAndGenresContaining(String title, String genres) {
                return List.of();
            }
        };
    }
}