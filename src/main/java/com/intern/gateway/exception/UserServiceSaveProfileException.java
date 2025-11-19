package com.intern.gateway.exception;

import lombok.Getter;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Getter
public class UserServiceSaveProfileException extends WebClientException {

    public UserServiceSaveProfileException(WebClientResponseException cause) {
        super("Failed to save profile in UserService ", cause);
    }
}
