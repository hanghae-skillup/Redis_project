package com.sparta.multimovieservice;

import com.sparta.multimovieservice.util.TestDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MultiMovieServiceApplicationTests {

	@Autowired
	private TestDataGenerator testDataGenerator;

	@Test
	void generateData() {
		testDataGenerator.generateTestData();
	}

}
