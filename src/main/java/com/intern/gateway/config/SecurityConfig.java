package com.intern.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/swagger-ui/**").permitAll()
                        .pathMatchers("/v3/api-docs/**").permitAll()
                        .pathMatchers("/login", "/register").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter())
                        ))
                )
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        scopeConverter.setAuthorityPrefix("SCOPE_");

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopeConverter.convert(jwt));
            authorities.addAll(extractRealmRoles(jwt));
            return authorities;
        });

        return converter;
    }

    private Collection<SimpleGrantedAuthority> extractRealmRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (!(realmAccess instanceof Map)) return Collections.emptyList();
        Map<String, Object> realm = (Map<String, Object>) realmAccess;
        Object roles = realm.get("roles");
        if (!(roles instanceof Collection)) return Collections.emptySet();
        return ((Collection<?>) roles).stream()
                .map(Object::toString)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toSet());
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        return NimbusReactiveJwtDecoder.withJwkSetUri(
                issuerUri + "/protocol/openid-connect/certs"
        ).build();
    }

}
