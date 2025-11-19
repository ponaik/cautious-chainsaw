package com.intern.gateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Getter
public class WebClientException extends RuntimeException {
    protected final HttpStatus status;
    protected final String details;

    public WebClientException(String message, WebClientResponseException cause) {
        super(message, cause);
        this.status = (HttpStatus) cause.getStatusCode();
        this.details = cause.getResponseBodyAsString();
    }

}
