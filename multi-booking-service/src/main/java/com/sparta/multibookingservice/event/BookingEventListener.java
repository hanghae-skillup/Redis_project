package com.sparta.multibookingservice.event;

import com.sparta.dto.booking.BookingResponseDto;
import com.sparta.multibookingservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingEventListener {
    private final MessageService messageService;

    @Async
    @EventListener
    public void handleBookingCompletedEvent(BookingCompletedEvent event) {
        String message = createBookingMessage(event);
        messageService.send(event.userId(), event.phoneNumber(), message);
    }

    private String createBookingMessage(BookingCompletedEvent event) {
        return String.format("Book is completed. Seats: %s",
                event.bookings().stream()
                        .map(BookingResponseDto::seatNumber)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("")
        );
    }
}