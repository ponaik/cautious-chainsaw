package com.intern.gateway.controller;

import com.intern.gateway.dto.LoginResponse;
import com.intern.gateway.dto.UserLoginRequest;
import com.intern.gateway.dto.UserRegistrationRequest;
import com.intern.gateway.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class GatewayController {

    private final GatewayService gatewayService;

    @Autowired
    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<Void>> register(@RequestBody UserRegistrationRequest request) {
        return gatewayService.registerUser(request)
                .map(saved -> ResponseEntity.ok().build());
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody UserLoginRequest request) {
        return gatewayService.loginUser(request)
                .map(token ->  ResponseEntity.ok().body(token));
    }
}