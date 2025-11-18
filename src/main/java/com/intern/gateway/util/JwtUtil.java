package com.intern.gateway.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtUtil {

    private final ReactiveJwtDecoder jwtDecoder;

    @Autowired
    public JwtUtil(ReactiveJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public Mono<String> getSubject(String token) {
        return jwtDecoder.decode(token)
                .map(Jwt::getSubject);
    }
}
