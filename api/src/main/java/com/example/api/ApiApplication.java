package com.example.api;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.example.domain",         // Domain 모듈의 패키지
		"com.example.infrastructure", // Infrastructure 모듈의 패키지
		"com.example.service"         // Service 모듈의 패키지
})
@EnableJpaRepositories(basePackages = "com.example.infrastructure.repository") // JpaMovieRepository가 있는 경로
@EntityScan(basePackages = "com.example.domain.entity") // 엔티티 경로
@ComponentScan(basePackages = "com.example")
@EnableJpaAuditing
public class ApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}
}
