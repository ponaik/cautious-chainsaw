package com.intern.gateway.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class UserServiceRollbackException extends WebClientException implements RollbackException {

    public UserServiceRollbackException(WebClientResponseException cause, Long id) {
        super("Failed to delete in UserService with id: " + id, cause);
    }
}
