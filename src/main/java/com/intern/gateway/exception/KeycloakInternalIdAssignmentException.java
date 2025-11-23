package com.intern.gateway.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class KeycloakInternalIdAssignmentException extends WebClientException {
    public KeycloakInternalIdAssignmentException(WebClientResponseException cause) {
        super("Failed to assign internalId in keycloak", cause);
    }
}
