package com.intern.gateway.service;

import com.intern.gateway.client.KeycloakAdminClient;
import com.intern.gateway.client.UserServiceClient;
import com.intern.gateway.dto.LoginResponse;
import com.intern.gateway.dto.UserLoginRequest;
import com.intern.gateway.dto.UserRegistrationRequest;
import com.intern.gateway.dto.UserResponse;
import com.intern.gateway.exception.PostRegistrationAuthenticationException;
import com.intern.gateway.exception.UserAuthenticationException;
import com.intern.gateway.exception.UserServiceSaveProfileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class GatewayService {

    private final KeycloakAdminClient keycloakAdminClient;
    private final UserServiceClient userServiceClient;

    @Autowired
    public GatewayService(KeycloakAdminClient keycloakAdminClient, UserServiceClient userServiceClient) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.userServiceClient = userServiceClient;
    }

    public Mono<UserResponse> registerUser(UserRegistrationRequest request) {
        return keycloakAdminClient.createUser(request)
                .flatMap(ignored -> keycloakAdminClient.authenticateUser(request.username(), request.password())
                        .onErrorResume(WebClientResponseException.class, err ->
                                Mono.error(new PostRegistrationAuthenticationException(err))))
                .flatMap(token -> userServiceClient.saveProfile(token, request)
                        .onErrorResume(WebClientResponseException.class, err ->
                                keycloakAdminClient.rollbackUser(token)
                                        .then(Mono.error(new UserServiceSaveProfileException(err)))
                        )
                );
    }


    public Mono<LoginResponse> loginUser(UserLoginRequest request) {
            return keycloakAdminClient.authenticateUser(request.username(), request.password())
                    .map(LoginResponse::new)
                    .onErrorResume(WebClientResponseException.class, err ->
                            Mono.error(new UserAuthenticationException(err)));
    }
}
