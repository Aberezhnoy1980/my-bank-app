package com.mybank.security.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class JwtUsernameResolverTest {

    private final JwtUsernameResolver resolver = new JwtUsernameResolver();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPreferPreferredUsernameClaim() {
        SecurityContextHolder.getContext().setAuthentication(jwtAuth("preferred_username", "alice.user", "sub-1"));

        assertThat(resolver.resolve("demo.user")).isEqualTo("alice.user");
    }

    @Test
    void shouldUseSubjectWhenPreferredUsernameMissing() {
        SecurityContextHolder.getContext().setAuthentication(jwtAuth("sub", "user-sub", "user-sub"));

        assertThat(resolver.resolve("demo.user")).isEqualTo("user-sub");
    }

    @Test
    void shouldUseFallbackWhenNoJwt() {
        assertThat(resolver.resolve("demo.user")).isEqualTo("demo.user");
    }

    private static JwtAuthenticationToken jwtAuth(String claimName, String claimValue, String subject) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(claimName, claimValue)
                .subject(subject)
                .build();
        return new JwtAuthenticationToken(jwt);
    }
}
