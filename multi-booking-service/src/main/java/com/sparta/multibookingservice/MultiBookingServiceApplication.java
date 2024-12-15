package com.sparta.multibookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = {"com.sparta.domain"})
@EnableJpaRepositories(basePackages = {"com.sparta.multibookingservice.repository", "com.sparta.repository"})
@SpringBootApplication
public class MultiBookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiBookingServiceApplication.class, args);
    }

}
