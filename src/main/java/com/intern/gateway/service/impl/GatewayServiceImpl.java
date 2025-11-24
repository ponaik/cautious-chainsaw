package com.intern.gateway.service.impl;

import com.intern.gateway.client.AuthenticationClient;
import com.intern.gateway.client.KeycloakAdminClient;
import com.intern.gateway.client.UserServiceClient;
import com.intern.gateway.dto.LoginResponse;
import com.intern.gateway.dto.UserLoginRequest;
import com.intern.gateway.dto.UserRegistrationRequest;
import com.intern.gateway.dto.UserResponse;
import com.intern.gateway.exception.RollbackKeycloakRegistration;
import com.intern.gateway.exception.RollbackUserRegistration;
import com.intern.gateway.exception.UserAuthenticationException;
import com.intern.gateway.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.URI;

@Service
public class GatewayServiceImpl implements GatewayService {

    private final KeycloakAdminClient keycloakAdminClient;
    private final UserServiceClient userServiceClient;
    private final AuthenticationClient authClient;

    @Autowired
    public GatewayServiceImpl(KeycloakAdminClient keycloakAdminClient, UserServiceClient userServiceClient, AuthenticationClient authClient) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.userServiceClient = userServiceClient;
        this.authClient = authClient;
    }

    @Override public Mono<UserResponse> registerUser(UserRegistrationRequest request) {
        return keycloakAdminClient.createUser(request)
                .map(this::getSubFromResponse)
                .flatMap(userSub -> saveUserProfile(userSub, request))
                .flatMap(tuple -> addKeycloakInternalId(tuple, request))
                .onErrorResume(this::handleRollbacks);
    }


    @Override public Mono<LoginResponse> loginUser(UserLoginRequest request) {
        return authClient.authenticateUser(request.username(), request.password())
                .map(LoginResponse::new)
                .onErrorResume(WebClientResponseException.class, err ->
                        Mono.error(new UserAuthenticationException(err)));
    }

    private Mono<UserResponse> handleRollbacks(Throwable cause) {
        if (cause instanceof RollbackKeycloakRegistration ex) {
            String userSub = ex.getUserSub();
            keycloakAdminClient.deleteUserBySub(userSub)
                    .subscribe();
        }
        if (cause instanceof RollbackUserRegistration ex) {
            Long userId = ex.getUserId();
            userServiceClient.deleteUserById(userId)
                    .subscribe();
        }

        return Mono.error(cause);
    }

    private String getSubFromResponse(ResponseEntity<Void> response) {
        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new IllegalStateException("Location header is missing in response");
        }
        String locationString = location.toString();
        return locationString.substring(locationString.lastIndexOf("/") + 1);
    }

    private Mono<Tuple2<UserResponse, String>> saveUserProfile(String userSub, UserRegistrationRequest request) {
        return userServiceClient.saveProfile(userSub, request)
                .map(userResponse -> Tuples.of(userResponse, userSub));
    }

    private Mono<UserResponse> addKeycloakInternalId(Tuple2<UserResponse, String> tuple, UserRegistrationRequest request) {
        UserResponse userResponse = tuple.getT1();
        String userSub = tuple.getT2();

        return keycloakAdminClient.addInternalId(userResponse.id(), request, userSub)
                .thenReturn(userResponse);
    }


}
