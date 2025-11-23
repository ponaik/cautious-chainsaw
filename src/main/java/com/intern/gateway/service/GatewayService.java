package com.intern.gateway.service;

import com.intern.gateway.client.KeycloakAdminClient;
import com.intern.gateway.client.UserServiceClient;
import com.intern.gateway.dto.LoginResponse;
import com.intern.gateway.dto.UserLoginRequest;
import com.intern.gateway.dto.UserRegistrationRequest;
import com.intern.gateway.dto.UserResponse;
import com.intern.gateway.exception.UserAuthenticationException;
import com.intern.gateway.exception.UserServiceSaveProfileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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
                .thenReturn(request)
                .flatMap(this::fetchUserSubAndToken)
                .flatMap(tuple -> saveProfileWithRollback(tuple, request))
                .flatMap(tuple -> addInternalId(tuple, request));
    }

    private Mono<Tuple2<String, String>> fetchUserSubAndToken(UserRegistrationRequest request) {
        return keycloakAdminClient.getUserSubByUsername(request.username())
                .zipWith(keycloakAdminClient.getAdminToken());
    }

    private Mono<Tuple2<UserResponse, String>> saveProfileWithRollback(Tuple2<String, String> tuple, UserRegistrationRequest request) {
        String userSub = tuple.getT1();
        String adminToken = tuple.getT2();

        return userServiceClient.saveProfile(userSub, adminToken, request)
                .onErrorResume(WebClientResponseException.class, err ->
                        keycloakAdminClient.rollbackUserBySub(userSub)
                                .then(Mono.error(new UserServiceSaveProfileException(err)))
                )
                .map(userResponse -> Tuples.of(userResponse, userSub));
    }

    private Mono<UserResponse> addInternalId(Tuple2<UserResponse, String> tuple, UserRegistrationRequest request) {
        UserResponse userResponse = tuple.getT1();
        String userSub = tuple.getT2();

        return keycloakAdminClient.addInternalId(userResponse, request, userSub);
    }



    public Mono<LoginResponse> loginUser(UserLoginRequest request) {
            return keycloakAdminClient.authenticateUser(request.username(), request.password())
                    .map(LoginResponse::new)
                    .onErrorResume(WebClientResponseException.class, err ->
                            Mono.error(new UserAuthenticationException(err)));
    }
}
