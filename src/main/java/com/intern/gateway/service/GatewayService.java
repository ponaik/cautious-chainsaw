package com.intern.gateway.service;

import com.intern.gateway.dto.LoginResponse;
import com.intern.gateway.dto.UserLoginRequest;
import com.intern.gateway.dto.UserRegistrationRequest;
import com.intern.gateway.dto.UserResponse;
import reactor.core.publisher.Mono;

public interface GatewayService {
    Mono<UserResponse> registerUser(UserRegistrationRequest request);

    Mono<LoginResponse> loginUser(UserLoginRequest request);
}
