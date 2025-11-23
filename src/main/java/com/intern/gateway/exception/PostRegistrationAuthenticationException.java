package com.intern.gateway.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class PostRegistrationAuthenticationException extends WebClientException {

    public PostRegistrationAuthenticationException(WebClientResponseException cause) {
        super("Failed to request User sub by username in Keycloak after registration", cause);
    }
}
