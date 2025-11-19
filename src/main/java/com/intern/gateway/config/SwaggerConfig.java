package com.intern.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String oauthIssuerUri) {
        return new OpenAPI()
                .info(new Info()
                        .title("Gateway")
                        .description("API Gateway Service")
                        .version("0.0.1-SNAPSHOT"))
                .addSecurityItem(new SecurityRequirement().addList("OAuth2Scheme"))
                .components(new Components()
                        .addSecuritySchemes("OAuth2Scheme",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .description("OAuth2 flow")
                                        .flows(new OAuthFlows()
                                                .authorizationCode(new OAuthFlow()
                                                        .authorizationUrl(oauthIssuerUri + "/protocol/openid-connect/auth")
                                                        .tokenUrl(oauthIssuerUri + "/protocol/openid-connect/token")))));
    }
}
