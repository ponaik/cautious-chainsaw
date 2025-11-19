package com.intern.gateway.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class UserAuthenticationException extends WebClientException {
    public UserAuthenticationException(WebClientResponseException cause) {
        super("Authentication failed", cause);
    }
}
