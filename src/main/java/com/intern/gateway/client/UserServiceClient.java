package com.intern.gateway.client;

import com.intern.gateway.dto.UserRegistrationRequest;
import com.intern.gateway.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    @Autowired
    public UserServiceClient(@Value("${network.user-service.base-url}") String userserviceBaseUrl,
                             WebClient.Builder builder) {
        this.webClient = builder.baseUrl(userserviceBaseUrl).build();
    }

    public Mono<UserResponse> saveProfile(String sub, String token, UserRegistrationRequest request) {
        return webClient.post()
                .uri("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(Map.of(
                        "username", request.username(),
                        "email", request.email(),
                        "name", request.name(),
                        "surname", request.surname(),
                        "birthDate", request.birthDate(),
                        "sub", sub
                ))
                .retrieve()
                .bodyToMono(UserResponse.class);
    }

}
