package com.intern.gateway.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
public class ErrorResponse implements Serializable {
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    @JsonRawValue
    private final String details;

    public ErrorResponse(HttpStatus status, String message, String path, String details) {
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }
}
