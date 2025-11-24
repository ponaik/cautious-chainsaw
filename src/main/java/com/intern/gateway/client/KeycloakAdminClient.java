package com.intern.gateway.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.intern.gateway.dto.UserRegistrationRequest;
import com.intern.gateway.exception.KeycloakInternalIdAssignmentException;
import com.intern.gateway.exception.KeycloakRollbackException;
import com.intern.gateway.exception.UserRegistrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KeycloakAdminClient {

    private final String authClientSecret;
    private final String authClientId;
    private final String serviceClientId;
    private final String serviceClientSecret;
    private final WebClient webClient;

    @Autowired
    public KeycloakAdminClient(@Value("${network.auth-service.authentication-client.id}") String authClientId,
                               @Value("${network.auth-service.authentication-client.secret}") String authClientSecret,
                               @Value("${network.auth-service.service-client.id}") String serviceClientId,
                               @Value("${network.auth-service.service-client.secret}") String serviceClientSecret,
                               @Value("${network.auth-service.base-url}") String baseUrl,
                               WebClient.Builder builder) {
        this.authClientSecret = authClientSecret;
        this.authClientId = authClientId;
        this.serviceClientId = serviceClientId;
        this.serviceClientSecret = serviceClientSecret;
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<ResponseEntity<Void>> createUser(UserRegistrationRequest request) {
        return getAdminToken()
                .flatMap(token -> webClient.post()
                        .uri("/admin/realms/UserService/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .bodyValue(Map.of("username", request.username()))
                        .retrieve()
                        .toBodilessEntity()
                        .onErrorResume(WebClientResponseException.class, err ->
                                Mono.error(new UserRegistrationException(err)))
                );
    }

    public Mono<ResponseEntity<Void>> addInternalId(Long id, UserRegistrationRequest request, String sub) {
        return getAdminToken()
                .flatMap(token -> webClient.put()
                        .uri("/admin/realms/UserService/users/{id}", sub)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .bodyValue(Map.of(
                                "username", request.username(),
                                "email", request.email(),
                                "firstName", request.name(),
                                "lastName", request.surname(),
                                "enabled", true,
                                "attributes", Map.of("internalId", id),
                                "credentials", List.of(Map.of(
                                        "type", "password",
                                        "value", request.password(),
                                        "temporary", false
                                ))
                        ))
                        .retrieve()
                        .toBodilessEntity()
                        .onErrorResume(WebClientResponseException.class, err ->
                                Mono.error(new KeycloakInternalIdAssignmentException(err, id, sub)))
                );
    }

    public Mono<ResponseEntity<Void>> deleteUserBySub(String sub) {
        return getAdminToken()
                .doOnTerminate(() -> log.debug("Rollback in Keycloak for sub: {}", sub))
                .flatMap(token -> webClient.delete()
                        .uri("/admin/realms/UserService/users/{id}", sub)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .toBodilessEntity()
                        .onErrorResume(WebClientResponseException.class, err ->
                                Mono.error(new KeycloakRollbackException(err, sub))
                        )
                );
    }

    public Mono<String> authenticateUser(String username, String password) {
        return webClient.post()
                .uri("/realms/UserService/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", authClientId)
                        .with("client_secret", authClientSecret)
                        .with("username", username)
                        .with("password", password))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("access_token").asText());
    }

    public Mono<String> getAdminToken() {
        return webClient.post()
                .uri("/realms/UserService/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", serviceClientId)
                        .with("client_secret", serviceClientSecret))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("access_token").asText());
    }

}
