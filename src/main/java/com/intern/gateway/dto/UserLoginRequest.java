package com.intern.gateway.dto;

public record UserLoginRequest(
        String username,
        String password
) {}
