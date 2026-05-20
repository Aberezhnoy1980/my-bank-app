package com.mybank.front.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Optional;
import org.springframework.security.oauth2.client.jackson2.OAuth2ClientJackson2Module;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Persists the OAuth2 authorization request in a short-lived cookie instead of the HTTP session.
 * Avoids {@code authorization_request_not_found} when the session cookie is lost behind Ingress
 * (wrong {@code baseUrl}, port mismatch, or missing forwarded headers).
 */
public final class CookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    static final String COOKIE_NAME = "OAUTH2_AUTH_REQUEST";

    private static final int COOKIE_TTL_SECONDS = 180;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new OAuth2ClientJackson2Module());

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return readCookie(request, COOKIE_NAME)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            deleteCookie(request, response, COOKIE_NAME);
            return;
        }
        writeCookie(response, COOKIE_NAME, serialize(authorizationRequest), COOKIE_TTL_SECONDS);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        deleteCookie(request, response, COOKIE_NAME);
        return authorizationRequest;
    }

    private Optional<String> readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private void writeCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        writeCookie(response, name, "", 0);
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(authorizationRequest);
            return Base64.getUrlEncoder().encodeToString(bytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize OAuth2AuthorizationRequest", ex);
        }
    }

    private OAuth2AuthorizationRequest deserialize(String cookie) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cookie);
            return objectMapper.readValue(bytes, OAuth2AuthorizationRequest.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize OAuth2AuthorizationRequest", ex);
        }
    }
}
