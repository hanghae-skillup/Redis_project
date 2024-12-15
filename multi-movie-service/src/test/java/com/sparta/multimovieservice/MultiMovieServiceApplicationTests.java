package com.sparta.multimovieservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MultiMovieServiceApplicationTests {

	@Autowired
	private TestDataGenerator testDataGenerator;

	@Autowired
	private TheaterTestDataGenerator theaterTestDataGenerator;


    @Test
	void generateMovieData() {
		testDataGenerator.generateMovieData();
	}
	@Test
	void generateTheaters() {
		theaterTestDataGenerator.generateAllTestData();
	}
	@Test
	void generateScreeningData() {
		theaterTestDataGenerator.generateAllScreeningData();
	}

}
