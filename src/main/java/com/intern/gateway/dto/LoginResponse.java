package com.intern.gateway.dto;

import java.io.Serializable;

public record LoginResponse(
        String token
) implements Serializable {}
