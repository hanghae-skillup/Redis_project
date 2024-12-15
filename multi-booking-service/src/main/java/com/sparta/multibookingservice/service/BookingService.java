package com.sparta.multibookingservice.service;

import com.sparta.domain.booking.Booking;
import com.sparta.domain.movie.Screening;
import com.sparta.domain.movie.Seat;
import com.sparta.dto.booking.BookingRequestDto;
import com.sparta.dto.booking.BookingResponseDto;
import com.sparta.exception.MovieException;
import com.sparta.multibookingservice.annotation.DistributedLock;
import com.sparta.multibookingservice.event.BookingCompletedEvent;
import com.sparta.multibookingservice.redis.DistributedLockTemplate;
import com.sparta.multibookingservice.repository.BookingJpaRepository;
import com.sparta.multibookingservice.repository.ScreeningJpaRepository;
import com.sparta.multibookingservice.repository.SeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private static final int MAX_BOOKING_SEATS = 5;

    private final BookingJpaRepository bookingJpaRepository;
    private final ScreeningJpaRepository screeningJpaRepository;
    private final SeatJpaRepository seatJpaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DistributedLockTemplate lockTemplate;

    public List<BookingResponseDto> book(BookingRequestDto request) {
        return lockTemplate.executeWithLock(
                "booking:" + request.screeningId(),
                0L,
                3L,
                TimeUnit.SECONDS,
                () -> processBooking(request)
        );
    }

    private List<BookingResponseDto> processBooking(BookingRequestDto request) {
        validateBookingRequest(request);

        Screening screening = screeningJpaRepository.findByIdWithTheaterAndMovie(request.screeningId())
                .orElseThrow(() -> new MovieException("movie not found."));

        List<Seat> seats = getSeatsByNumbers(screening.getTheater().getId(), request.seatNumbers());

        if (seats.stream().anyMatch(Seat::isBooked)) {
            throw new MovieException("Seat is already booked.");
        }

        seats.forEach(Seat::book);

        List<Booking> bookings = createBookings(request, screening, seats);
        bookingJpaRepository.saveAll(bookings);
        List<BookingResponseDto> responses = bookings.stream()
                .map(BookingResponseDto::from)
                .toList();

        eventPublisher.publishEvent(new BookingCompletedEvent(
                request.userId(),
                request.phoneNumber(),
                responses
        ));
        return responses;
    }

//    // Distributed Lock (AOP)
//    @DistributedLock(key = "booking",
//            waitTime = 0,
//            leaseTime = 3,
//            timeUnit = TimeUnit.SECONDS
//    )
//    public List<BookingResponseDto> book(BookingRequestDto request) {
//        validateBookingRequest(request);
//
//        Screening screening = screeningJpaRepository.findByIdWithTheaterAndMovie(request.screeningId())
//                .orElseThrow(() -> new MovieException("movie not found."));
//
//        List<Seat> seats = getSeatsByNumbers(screening.getTheater().getId(), request.seatNumbers());
//
//        List<Booking> bookings = createBookings(request, screening, seats);
//        bookingJpaRepository.saveAll(bookings);
//
//        List<BookingResponseDto> responses = bookings.stream()
//                .map(BookingResponseDto::from)
//                .toList();
//
//        eventPublisher.publishEvent(new BookingCompletedEvent(
//                request.userId(),
//                request.phoneNumber(),
//                responses
//        ));
//
//        return responses;
//    }
      // 기존 로직
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public List<BookingResponseDto> book(BookingRequestDto request) {
//        validateBookingRequest(request);
//
//        Screening screening = screeningJpaRepository.findByIdWithTheaterAndMovie(request.screeningId())
//                .orElseThrow(() -> new MovieException("movie not found."));
//
//        List<Seat> seats = getSeatsByNumbers(screening.getTheater().getId(), request.seatNumbers());
//        validateSeatAvailability(screening, seats);
//
//        List<Booking> bookings = createBookings(request, screening, seats);
//        bookingJpaRepository.saveAll(bookings);
//
//        List<BookingResponseDto> responses = bookings.stream()
//                .map(BookingResponseDto::from)
//                .toList();
//
//        eventPublisher.publishEvent(new BookingCompletedEvent(
//                request.userId(),
//                request.phoneNumber(),
//                responses
//        ));
//
//        return responses;
//    }

    private void validateBookingRequest(BookingRequestDto request) {
        if (request.seatNumbers().isEmpty()) {
            throw new MovieException("Please selected seat.");
        }

        if (request.seatNumbers().size() > MAX_BOOKING_SEATS) {
            throw new MovieException("Only " + MAX_BOOKING_SEATS + "seats available.");
        }

        validateSeatsAreConsecutive(request.seatNumbers());

        int existingBookings = bookingJpaRepository.countByScreeningIdAndUserId(
                request.screeningId(), request.userId());
        if (existingBookings + request.seatNumbers().size() > MAX_BOOKING_SEATS) {
            throw new MovieException("Only " + MAX_BOOKING_SEATS + "seats available each one movie.");
        }
    }

    private void validateSeatsAreConsecutive(List<String> seatNumbers) {
        if (!areSeatsConsecutive(seatNumbers)) {
            throw new MovieException("Seats must be consecutive.");
        }
    }

    private boolean areSeatsConsecutive(List<String> seatNumbers) {
        if (seatNumbers.size() <= 1) return true;

        String firstSeatRow = String.valueOf(seatNumbers.get(0).charAt(0));
        boolean isSameRow = seatNumbers.stream()
                .allMatch(seat -> seat.startsWith(firstSeatRow));

        if (!isSameRow) return false;

        List<Integer> columns = seatNumbers.stream()
                .map(s -> Integer.parseInt(s.substring(1)))
                .sorted()
                .toList();

        for (int i = 0; i < columns.size() - 1; i++) {
            if (columns.get(i + 1) - columns.get(i) != 1) {
                return false;
            }
        }
        return true;
    }

    private List<Seat> getSeatsByNumbers(Integer theaterId, List<String> seatNumbers) {
        if (theaterId == null) {
            throw new MovieException("Theater ID is null.");
        }

        return seatNumbers.stream()
                .map(seatNumber -> {
                    String row = String.valueOf(seatNumber.charAt(0));
                    int column = Integer.parseInt(seatNumber.substring(1));
                    return seatJpaRepository.findByTheaterIdAndSeatRowAndSeatColumn(theaterId, row, column)
                            .orElseThrow(() -> new MovieException("seat is not exist: " + seatNumber));
                })
                .toList();
    }

//    // Optimistic lock (공유자원: seat에 대한 version 관리)
//    private List<Seat> getSeatsByNumbers(Integer theaterId, List<String> seatNumbers) {
//        if (theaterId == null) {
//            throw new MovieException("Theater ID is null.");
//        }
//
//        return seatNumbers.stream()
//                .map(seatNumber -> {
//                    String row = String.valueOf(seatNumber.charAt(0));
//                    int column = Integer.parseInt(seatNumber.substring(1));
//                    Seat seat = seatJpaRepository.findByTheaterIdAndSeatRowAndSeatColumnWithLock(theaterId, row, column)
//                            .orElseThrow(() -> new MovieException("seat is not exist: " + seatNumber));
//
//                    if (seat.isBooked()) {
//                        throw new MovieException("Seat is already booked: " + seatNumber);
//                    }
//                    seat.book();  // 예약 상태로 변경
//                    return seat;
//                })
//                .toList();
//    }

    private void validateSeatAvailability(Screening screening, List<Seat> seats) {
        List<Seat> bookedSeats = bookingJpaRepository.findByScreening(screening)
                .stream()
                .map(Booking::getSeat)
                .toList();

        boolean hasConflict = seats.stream().anyMatch(bookedSeats::contains);
        if (hasConflict) {
            throw new MovieException("Seat is already booked.");
        }
    }

    // pessimistic lock
//    private void validateSeatAvailability(Screening screening, List<Seat> seats) {
//        List<Seat> bookedSeats = bookingJpaRepository.findByScreeningWithPessimisticLock(screening)
//                .stream()
//                .map(Booking::getSeat)
//                .toList();
//
//        boolean hasConflict = seats.stream().anyMatch(bookedSeats::contains);
//        if (hasConflict) {
//            throw new MovieException("Seat is already booked.");
//        }
//    }


    private List<Booking> createBookings(BookingRequestDto request, Screening screening, List<Seat> seats) {
        return seats.stream()
                .map(seat -> Booking.builder()
                        .screening(screening)
                        .seat(seat)
                        .userId(request.userId())
                        .phoneNumber(request.phoneNumber())
                        .build())
                .toList();
    }
}