package com.sparta.domain.movie;

import com.sparta.domain.theater.Theater;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @Column(nullable = false)
    private String seatRow;

    @Column(name = "seat_column", nullable = false)
    private Integer seatColumn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_booked", nullable = false)
    private Boolean isBooked = false;

    @Version
    private Long version;

    @Builder
    public Seat(Theater theater, String seatRow, Integer seatColumn) {
        this.theater = theater;
        this.seatRow = seatRow;
        this.seatColumn = seatColumn;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void book() {
        this.isBooked = true;
    }

    public Boolean isBooked() {
        return isBooked;
    }
}