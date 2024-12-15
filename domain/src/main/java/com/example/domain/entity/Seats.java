package com.example.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Seats extends Timestamped{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long id;

    private String seatNumber;
    private Long movieId;
    private Boolean isReserved;
    private Long reservationId;


    @Version // Optimistic Lock 구현
    private Long version; // JPA가 관리하는 버전 필드

    public void updateReservation(Long userId, Long reservationId){
        this.isReserved = true;
        this.reservationId = reservationId;
        this.updateBy(userId);
    }



}
