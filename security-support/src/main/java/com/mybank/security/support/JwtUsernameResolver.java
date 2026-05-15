package com.mybank.security.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Resolves the current username from JWT claims ({@code preferred_username}, then {@code sub})
 * or an explicit fallback when security is disabled or the token has no usable claims.
 */
public class JwtUsernameResolver {

    public String resolve(String fallbackUsername) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            String preferred = jwt.getToken().getClaimAsString("preferred_username");
            if (preferred != null && !preferred.isBlank()) {
                return preferred;
            }
            String sub = jwt.getToken().getSubject();
            if (sub != null && !sub.isBlank()) {
                return sub;
            }
        }
        if (fallbackUsername != null && !fallbackUsername.isBlank()) {
            return fallbackUsername;
        }
        throw new IllegalStateException("No username in security context and no fallback configured");
    }
}
