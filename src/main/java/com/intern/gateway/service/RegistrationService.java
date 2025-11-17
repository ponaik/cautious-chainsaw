package com.intern.gateway.service;

import com.intern.gateway.dto.UserRegistrationRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class RegistrationService {

    private final WebClient keycloakClient;
    private final WebClient userServiceClient;

    public RegistrationHandler(WebClient.Builder builder) {
        this.keycloakClient = builder.baseUrl("http://keycloak:8080").build();
        this.userServiceClient = builder.baseUrl("http://user-service:8080").build();
    }

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(UserRegistrationRequest.class)
                .flatMap(reg -> createKeycloakUser(reg)
                        .flatMap(token -> createUserServiceEntry(reg, token)
                                .onErrorResume(err -> rollbackKeycloakUser(reg)
                                        .then(Mono.error(err))
                                )
                        )
                )
                .flatMap(token -> ServerResponse.ok().bodyValue(Map.of("token", token)))
                .onErrorResume(err -> ServerResponse.badRequest().bodyValue(err.getMessage()));
    }

    private Mono<String> createKeycloakUser(UserRegistrationRequest reg) {
        // Call Keycloak Admin REST API to create user
        return keycloakClient.post()
                .uri("/auth/admin/realms/myrealm/users")
                .bodyValue(reg.toKeycloakUser())
                .retrieve()
                .bodyToMono(Void.class)
                .then(getToken(reg)); // exchange credentials for token
    }

    private Mono<String> getToken(UserRegistrationRequest reg) {
        return keycloakClient.post()
                .uri("/auth/realms/myrealm/protocol/openid-connect/token")
                .bodyValue(reg.toTokenRequest())
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::getAccessToken);
    }

    private Mono<Void> createUserServiceEntry(UserRegistrationRequest reg, String token) {
        return userServiceClient.post()
                .uri("/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(reg.toUserServicePayload())
                .retrieve()
                .bodyToMono(Void.class);
    }

    private Mono<Void> rollbackKeycloakUser(UserRegistrationRequest reg) {
        return keycloakClient.delete()
                .uri("/auth/admin/realms/myrealm/users/{id}", reg.getKeycloakId())
                .retrieve()
                .bodyToMono(Void.class);
    }
}

