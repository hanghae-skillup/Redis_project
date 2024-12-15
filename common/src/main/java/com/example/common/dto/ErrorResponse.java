package com.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter  @Builder
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timeStamp;
    private String errorCode;
    private String message;
    private String description;
    private StringBuffer path;

    public ErrorResponse(String errorCode, String message, String description, StringBuffer path){
        this.timeStamp = LocalDateTime.now();
        this.errorCode = errorCode;
        this.message = message;
        this.description = description;
        this.path = path;
    }

    public static ErrorResponse createInternalServerError(StringBuffer path){
        return new ErrorResponse("500", "Internal Server Error", "일시적인 서버 오류입니다. 해당 오류는 담당자에게 자동 보고되었습니다.", path);
    }

    public static ErrorResponse createNotMethodNotAllowed(StringBuffer path){
        return new ErrorResponse("405","Method Not Allowed", "제공하지 않는 요청방식입니다.", path);
    }
}
