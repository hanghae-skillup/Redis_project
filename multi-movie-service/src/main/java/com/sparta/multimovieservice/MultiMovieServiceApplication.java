package com.sparta.multimovieservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.sparta.domain"})  // Movie 엔티티가 있는 패키지
@EnableJpaRepositories(basePackages = {"com.sparta.multimovieservice.impl", "com.sparta.repository"})  // 레포지토리가 있는 패키지
public class MultiMovieServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MultiMovieServiceApplication.class, args);
	}
}