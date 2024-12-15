package com.sparta.multibookingservice;

import com.sparta.dto.booking.BookingRequestDto;
import com.sparta.multibookingservice.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookingService;

    @Test
    @DisplayName("비관적 락을 사용한 동시 예약 테스트")
    void pessimistic_lock_test() {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // DB 데이터 입력
        BookingRequestDto request = new BookingRequestDto(
                "user10",
                3,
                "010-1234-5678",
                Arrays.asList("A1", "A2", "A3")
        );

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    bookingService.book(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("Booking failed: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        executorService.shutdown();

        assertAll(
                () -> assertThat(successCount.get()).isEqualTo(1),
                () -> assertThat(failCount.get()).isEqualTo(numberOfThreads - 1)
        );
    }
}
