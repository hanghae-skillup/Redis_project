package com.sparta.multimovieservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.sparta.domain"})
@EnableJpaRepositories(basePackages = {"com.sparta.multimovieservice.repository", "com.sparta.repository"})
@ComponentScan(basePackages = {"com.sparta.multimovieservice", "com.sparta.multimovieservice.service"})
public class MultiMovieServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultiMovieServiceApplication.class, args);
    }
}