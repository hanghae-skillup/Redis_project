package com.example.common.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Builder
@Getter @ToString
public class MessageResponse  {
    private final String message;

    @JsonCreator
    public MessageResponse(@JsonProperty("message") String message) {
        this.message = message;
    }
}
