package com.intern.gateway.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.intern.gateway.dto.UserRegistrationRequest;
import com.intern.gateway.exception.UserRegistrationException;
import com.intern.gateway.util.JwtUtil;
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
    private final JwtUtil jwtUtil;

    @Autowired
    public KeycloakAdminClient(@Value("${network.auth-service.authentication-client.id}") String authClientId,
                               @Value("${network.auth-service.authentication-client.secret}") String authClientSecret,
                               @Value("${network.auth-service.service-client.id}") String serviceClientId,
                               @Value("${network.auth-service.service-client.secret}") String serviceClientSecret,
                               @Value("${network.auth-service.base-url}") String baseUrl,
                               WebClient.Builder builder,
                               JwtUtil jwtUtil) {
        this.authClientSecret = authClientSecret;
        this.authClientId = authClientId;
        this.serviceClientId = serviceClientId;
        this.serviceClientSecret = serviceClientSecret;
        this.webClient = builder.baseUrl(baseUrl).build();
        this.jwtUtil = jwtUtil;
    }

    public Mono<ResponseEntity<Void>> createUser(UserRegistrationRequest request) {
        return getAdminToken()
                .flatMap(token -> webClient.post()
                        .uri("/admin/realms/UserService/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .bodyValue(Map.of(
                                "username", request.username(),
                                "email", request.email(),
                                "firstName", request.name(),
                                "lastName", request.surname(),
                                "enabled", true,
                                "credentials", List.of(Map.of(
                                        "type", "password",
                                        "value", request.password(),
                                        "temporary", false
                                ))
                        ))
                        .retrieve()
                        .toBodilessEntity()
                        .onErrorResume(WebClientResponseException.class, err ->
                                Mono.error(new UserRegistrationException(err)))
                );
    }

    public Mono<ResponseEntity<Void>> rollbackUser(String userToken) {
        return getAdminToken()
                .flatMap(token -> jwtUtil.getSubject(userToken)
                        .flatMap(sub -> {
                            log.debug("Executing rollback for user {}", sub);
                            return webClient.delete()
                                    .uri("/admin/realms/UserService/users/{id}", sub)
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                    .retrieve()
                                    .toBodilessEntity();
                        })
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

    private Mono<String> getAdminToken() {
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
