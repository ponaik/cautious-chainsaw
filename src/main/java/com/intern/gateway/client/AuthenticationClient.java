package com.intern.gateway.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationClient {

    private final String authClientSecret;
    private final String authClientId;
    private final String serviceClientId;
    private final String serviceClientSecret;
    private final WebClient webClient;

    @Autowired
    public AuthenticationClient(@Value("${network.auth-service.authentication-client.id}") String authClientId,
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

    public Mono<String> authenticateAdminService() {
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
