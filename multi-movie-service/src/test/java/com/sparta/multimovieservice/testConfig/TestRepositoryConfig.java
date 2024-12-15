package com.sparta.multimovieservice.testConfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaRepositories(basePackages = "com.sparta.multimovieservice.testConfig.repository")
public class TestRepositoryConfig {
}