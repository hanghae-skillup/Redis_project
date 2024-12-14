package com.sparta.multimovieservice.domain.reservation;

import com.sparta.multimovieservice.domain.movie.Screening;
import com.sparta.multimovieservice.domain.theater.Seat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Reservation(Screening screening, Seat seat, String customerName, String phoneNumber) {
        this.screening = screening;
        this.seat = seat;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}