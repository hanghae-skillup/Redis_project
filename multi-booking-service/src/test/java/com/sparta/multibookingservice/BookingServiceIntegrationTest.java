package com.sparta.multibookingservice;

import com.sparta.dto.booking.BookingRequestDto;
import com.sparta.exception.MovieException;
import com.sparta.multibookingservice.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@EnableAspectJAutoProxy
@SpringBootTest
@DirtiesContext
@TestPropertySource(properties = {
        "logging.level.org.hibernate.type.descriptor.sql=trace"
})
class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private RedissonClient redissonClient;

//    @Test
//    @DisplayName("비관적 락을 사용한 동시 예약 테스트")
//    void pessimistic_lock_test() {
//        int numberOfThreads = 10;
//        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
//        CountDownLatch latch = new CountDownLatch(numberOfThreads);
//        AtomicInteger successCount = new AtomicInteger();
//        AtomicInteger failCount = new AtomicInteger();
//
//        // DB 데이터 입력
//        BookingRequestDto request = new BookingRequestDto(
//                "user10",
//                3,
//                "010-1234-5678",
//                Arrays.asList("A1", "A2", "A3")
//        );
//
//        for (int i = 0; i < numberOfThreads; i++) {
//            executorService.submit(() -> {
//                try {
//                    bookingService.book(request);
//                    successCount.incrementAndGet();
//                } catch (Exception e) {
//                    System.out.println("Booking failed: " + e.getMessage());
//                    failCount.incrementAndGet();
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        executorService.shutdown();
//
//        assertAll(
//                () -> assertThat(successCount.get()).isEqualTo(1),
//                () -> assertThat(failCount.get()).isEqualTo(numberOfThreads - 1)
//        );
//    }

//    @Test
//    @Transactional
//    @DisplayName("낙관적 락을 사용한 동시 예약 테스트")
//    void optimistic_lock_test() throws InterruptedException {
//        int numberOfThreads = 10;
//        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
//        CountDownLatch latch = new CountDownLatch(numberOfThreads);
//        AtomicInteger successCount = new AtomicInteger();
//        AtomicInteger failCount = new AtomicInteger();
//
//        BookingRequestDto request = new BookingRequestDto(
//                "user5",
//                5,
//                "010-1234-5678",
//                Arrays.asList("A1")
//        );
//
//        for (int i = 0; i < numberOfThreads; i++) {
//            executorService.submit(() -> {
//                try {
//                    bookingService.book(request);
//                    System.out.println("Booking succeeded");
//                    successCount.incrementAndGet();
//                } catch (Exception e) {
//                    System.out.println("Booking failed: " + e.getMessage());
//                    failCount.incrementAndGet();
//                } finally {
//                    latch.countDown();
//                }
//            });
//            Thread.sleep(100);  // 약간의 간격을 두어 동시성 시뮬레이션
//        }
//
//        latch.await(10, TimeUnit.SECONDS);
//        executorService.shutdown();
//        executorService.awaitTermination(10, TimeUnit.SECONDS);
//
//        assertAll(
//                () -> assertThat(successCount.get()).isEqualTo(1),
//                () -> assertThat(failCount.get()).isEqualTo(numberOfThreads - 1)
//        );
//    }
//    @Test
//    @DisplayName("분산 락을 사용한 동시 예약 테스트")
//    void distributed_lock_test() throws InterruptedException {
//        int numberOfThreads = 10;
//        CountDownLatch startLatch = new CountDownLatch(1);
//        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
//        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
//        AtomicInteger successCount = new AtomicInteger();
//        AtomicInteger failCount = new AtomicInteger();
//
//        BookingRequestDto request = new BookingRequestDto(
//                "user1",
//                1,
//                "010-1234-5678",
//                Arrays.asList("A1", "A2", "A3")
//        );
//
//        for (int i = 0; i < numberOfThreads; i++) {
//            executorService.submit(() -> {
//                try {
//                    startLatch.await();
//                    bookingService.book(request);
//                    successCount.incrementAndGet();
//                } catch (Exception e) {
//                    failCount.incrementAndGet();
//                } finally {
//                    endLatch.countDown();
//                }
//            });
//        }
//
//        startLatch.countDown();
//        boolean await = endLatch.await(20, TimeUnit.SECONDS);
//        executorService.shutdownNow();
//
//        assertThat(await).isTrue();
//        assertThat(successCount.get()).isEqualTo(1);
//        assertThat(failCount.get()).isEqualTo(numberOfThreads - 1);
//    }
//    @AfterEach
//    void tearDown() throws Exception {
//        redissonClient.shutdown();
//    }
    @Test
    @DisplayName("함수형 분산 락을 사용한 동시 예약 테스트")
    void functional_distributed_lock_test() throws InterruptedException {
        int numberOfThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        BookingRequestDto request = new BookingRequestDto(
                "user1",
                1,
                "010-1234-5678",
                Arrays.asList("A1", "A2", "A3")
        );

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    bookingService.book(request);
                    System.out.println("Booking succeeded");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("Booking failed: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdownNow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(numberOfThreads - 1);
    }

    @AfterEach
    void tearDown() {
        redissonClient.shutdown();
    }
}

