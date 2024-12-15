package com.sparta.multibookingservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageService {
    public void send(String userId, String phoneNumber, String message) {
        log.info("Sending FCM message to user: {}, phone: {}, message: {}",
                userId, phoneNumber, message);
    }
}