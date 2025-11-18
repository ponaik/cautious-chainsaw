package com.intern.gateway.service;

import com.intern.gateway.client.KeycloakAdminClient;
import com.intern.gateway.client.UserServiceClient;
import com.intern.gateway.dto.UserLoginRequest;
import com.intern.gateway.dto.UserRegistrationRequest;
import com.intern.gateway.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
                .flatMap(ignored -> keycloakAdminClient.authenticateUser(request.username(), request.password()))
                .flatMap(token -> userServiceClient.saveProfile(token, request)
                        .onErrorResume(err -> keycloakAdminClient.rollbackUser(token)
                                .then(Mono.error(err))
                        )
                );
    }

    public Mono<String> loginUser(UserLoginRequest request) {
            return keycloakAdminClient.authenticateUser(request.username(), request.password());
    }
}
