package com.intern.gateway.exception;

import lombok.Getter;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Getter
public class KeycloakInternalIdAssignmentException
        extends WebClientException
        implements RollbackKeycloakRegistration, RollbackUserRegistration {

    private final Long userId;
    private final String userSub;

    public KeycloakInternalIdAssignmentException(WebClientResponseException cause, Long userId, String userSub) {
        super("Failed to assign internalId in keycloak", cause);
        this.userId = userId;
        this.userSub = userSub;
    }
}
