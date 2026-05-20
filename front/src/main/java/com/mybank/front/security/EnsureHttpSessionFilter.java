package com.mybank.front.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Creates the HTTP session before the OAuth2 authorization redirect so {@code JSESSIONID}
 * is set on the first response (Ingress / multi-hop redirects may otherwise drop state).
 */
final class EnsureHttpSessionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (requiresSession(request)) {
            request.getSession(true);
        }
        filterChain.doFilter(request, response);
    }

    private static boolean requiresSession(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if ("/".equals(uri)) {
            return true;
        }
        return uri.startsWith("/oauth2/") || uri.startsWith("/login/oauth2/");
    }
}
