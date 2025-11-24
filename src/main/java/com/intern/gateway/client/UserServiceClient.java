package com.intern.gateway.client;

import com.intern.gateway.dto.UserRegistrationRequest;
import com.intern.gateway.dto.UserResponse;
import com.intern.gateway.exception.UserServiceRollbackException;
import com.intern.gateway.exception.UserServiceSaveProfileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
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
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, err ->
                        Mono.error(new UserServiceSaveProfileException(err, sub))
                );
    }

    public Mono<ResponseEntity<Void>> deleteUserById(Long userId, String token) {
        return webClient.delete()
                .uri("/api/users/{id}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .doOnTerminate(() -> log.debug("Rollback in UserService for userId: {}", userId))
                .onErrorResume(WebClientResponseException.class, err ->
                        Mono.error(new UserServiceRollbackException(err, userId))
                );
    }
}
