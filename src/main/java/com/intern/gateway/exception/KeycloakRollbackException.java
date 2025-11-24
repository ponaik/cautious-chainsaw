package com.intern.gateway.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class KeycloakRollbackException extends WebClientException implements RollbackException {

    public KeycloakRollbackException(WebClientResponseException cause, String sub) {
        super("Failed to delete in Keycloak with sub: " + sub, cause);
    }
}

