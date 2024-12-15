package com.example.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity @Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Reservations extends Timestamped{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    private Long userId;
    private Long movieId;
    private Long seatId;
    private LocalDate reservedDate;


}
