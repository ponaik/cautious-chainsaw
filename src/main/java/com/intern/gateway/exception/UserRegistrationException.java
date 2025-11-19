package com.intern.gateway.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class UserRegistrationException extends WebClientException {

    public UserRegistrationException(WebClientResponseException cause) {
        super("Failed to register user in Keycloak", cause);
    }
}
