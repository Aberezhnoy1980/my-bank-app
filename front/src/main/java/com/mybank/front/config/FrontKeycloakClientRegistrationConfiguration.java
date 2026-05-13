package com.mybank.front.config;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * Keycloak в Docker: token/jwk по {@code http://keycloak:8080}, а claim {@code iss} в id/access token —
 * {@code http://localhost:8090/realms/mybank} ({@code KC_HOSTNAME} в Keycloak). OIDC discovery по внутреннему issuer
 * даёт в metadata другой {@code issuer} → Spring отвергает id_token ({@code [invalid_id_token]}).
 * <p>
 * Здесь регистрация собирается вручную: ожидаемый issuer для OIDC — только публичный URL; HTTP к token/jwk —
 * из переменных окружения.
 */
@Configuration
@Profile("secure")
public class FrontKeycloakClientRegistrationConfiguration {

    @Bean
    @Primary
    ClientRegistrationRepository clientRegistrationRepository(
            @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}") String clientSecret,
            @Value("${spring.security.oauth2.client.registration.keycloak.redirect-uri:{baseUrl}/login/oauth2/code/{registrationId}}")
            String redirectUri,
            @Value("${spring.security.oauth2.client.registration.keycloak.scope:openid,profile,offline_access}")
            String scopeLine,
            @Value("${app.oauth2.login-registration-id:keycloak}") String registrationId,
            @Value("${KEYCLOAK_OIDC_ISSUER_URI:http://localhost:8090/realms/mybank}") String tokenIssuerUri,
            @Value("${KEYCLOAK_AUTHORIZATION_URI:http://localhost:8090/realms/mybank/protocol/openid-connect/auth}")
            String authorizationUri,
            @Value("${KEYCLOAK_TOKEN_URI:http://localhost:8090/realms/mybank/protocol/openid-connect/token}")
            String tokenUri,
            @Value("${KEYCLOAK_JWK_SET_URI:http://localhost:8090/realms/mybank/protocol/openid-connect/certs}")
            String jwkSetUri,
            @Value("${spring.security.oauth2.client.provider.keycloak.user-name-attribute:preferred_username}")
            String userNameAttribute
    ) {
        Set<String> scopes = Arrays.stream(scopeLine.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        ClientRegistration registration = ClientRegistration.withRegistrationId(registrationId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .scope(scopes)
                .issuerUri(tokenIssuerUri)
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .jwkSetUri(jwkSetUri)
                .userNameAttributeName(userNameAttribute)
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }
}
