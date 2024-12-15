package com.sparta.multimovieservice;

import com.sparta.domain.movie.Movie;
import com.sparta.domain.movie.Screening;
import com.sparta.domain.movie.Seat;
import com.sparta.domain.theater.Theater;
import com.sparta.multimovieservice.testConfig.TestRepositoryConfig;
import com.sparta.multimovieservice.testConfig.repository.TestScreeningRepository;
import com.sparta.multimovieservice.testConfig.repository.TestTheaterRepository;
import com.sparta.repository.movie.MovieRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Import(TestRepositoryConfig.class)
@Component
@Slf4j
@RequiredArgsConstructor
public class TheaterTestDataGenerator {
    private final MovieRepository movieJpaRepository;
    private final TestTheaterRepository testTheaterRepository;
    private final TestScreeningRepository screeningRepository;


    private static final int NUMBER_OF_THEATERS = 5;
    private static final String[] ROWS = {"A", "B", "C", "D", "E"};
    private static final int SEATS_PER_ROW = 5;

    private static final LocalTime FIRST_SHOWING_TIME = LocalTime.of(10, 0);
    private static final LocalTime LAST_SHOWING_TIME = LocalTime.of(22, 0);
    private static final int DAYS_TO_GENERATE = 7;

    @Transactional
    public void generateTheaterData() {
        List<Theater> theaters = new ArrayList<>();

        for (int i = 1; i <= NUMBER_OF_THEATERS; i++) {
            Theater theater = Theater.builder()
                    .name(i + "관")
                    .build();

            for (String row : ROWS) {
                for (int seatNum = 1; seatNum <= SEATS_PER_ROW; seatNum++) {
                    Seat seat = Seat.builder()
                            .theater(theater)
                            .seatRow(row)
                            .seatColumn(seatNum)
                            .build();
                    theater.getSeats().add(seat);
                }
            }

            theaters.add(theater);
        }

        testTheaterRepository.saveAll(theaters);
        log.info("Generated {} theaters with {} seats each",
                theaters.size(),
                ROWS.length * SEATS_PER_ROW);
    }

    @Transactional
    public void generateScreeningTestData() {
        List<Movie> movies = movieJpaRepository.findAll();
        List<Theater> theaters = testTheaterRepository.findAll();

        if (movies.isEmpty() || theaters.isEmpty()) {
            log.error("No movies or theaters found in database");
            return;
        }

        List<Screening> screenings = new ArrayList<>();
        LocalDateTime currentDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Random random = new Random();

        for (int day = 0; day < DAYS_TO_GENERATE; day++) {
            LocalDateTime dateForSchedule = currentDate.plusDays(day);

            for (Theater theater : theaters) {
                LocalDateTime currentShowTime = dateForSchedule.with(FIRST_SHOWING_TIME);

                while (!currentShowTime.toLocalTime().isAfter(LAST_SHOWING_TIME)) {
                    Movie movie = movies.get(random.nextInt(movies.size()));
                    LocalDateTime endTime = currentShowTime.plusMinutes(movie.getDuration());

                    Screening screening = Screening.builder()
                            .movie(movie)
                            .theater(theater)
                            .startTime(currentShowTime)
                            .endTime(endTime)
                            .build();

                    screenings.add(screening);
                    currentShowTime = endTime.plusMinutes(30);
                }
            }
        }

        screeningRepository.saveAll(screenings);
        log.info("Generated {} screening schedules for {} days", screenings.size(), DAYS_TO_GENERATE);
    }

    @Transactional
    public void generateAllTestData() {
        generateTheaterData();
    }

    @Transactional
    public void generateAllScreeningData() {
        generateScreeningTestData();
    }
}