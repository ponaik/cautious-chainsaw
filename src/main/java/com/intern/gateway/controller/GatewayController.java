package com.intern.gateway.controller;

import com.intern.gateway.dto.LoginResponse;
import com.intern.gateway.dto.UserLoginRequest;
import com.intern.gateway.dto.UserRegistrationRequest;
import com.intern.gateway.dto.UserResponse;
import com.intern.gateway.service.GatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
@Tag(name = "Gateway", description = "Gateway API for user authentication and system registration.")
public class GatewayController {

    private final GatewayService gatewayService;

    @Autowired
    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Registers a new user profile across the identity provider (Keycloak) and the internal User Service.")
    public Mono<ResponseEntity<UserResponse>> register(@RequestBody UserRegistrationRequest request) {
        return gatewayService.registerUser(request)
                .map(response -> ResponseEntity.ok().body(response));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user credentials and returns a valid JWT access token.")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody UserLoginRequest request) {
        return gatewayService.loginUser(request)
                .map(token ->  ResponseEntity.ok().body(token));
    }
}