package com.example.api.controller;


import com.example.common.dto.MessageResponse;
import com.example.common.dto.ReservationRequest;
import com.example.service.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("")
    public ResponseEntity<MessageResponse> reserveSeats(@RequestBody ReservationRequest request) {
        reservationService.reserveSeats(request);
        return ResponseEntity.ok(new MessageResponse("예약이 완료되었습니다."));
    }
}