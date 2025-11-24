package com.intern.gateway.exception;

import lombok.Getter;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Getter
public class UserServiceSaveProfileException
        extends WebClientException
        implements RollbackKeycloakRegistration {

    private final String userSub;

    public UserServiceSaveProfileException(WebClientResponseException cause, String userSub) {
        super("Failed to save profile in UserService ", cause);
        this.userSub = userSub;
    }
}
