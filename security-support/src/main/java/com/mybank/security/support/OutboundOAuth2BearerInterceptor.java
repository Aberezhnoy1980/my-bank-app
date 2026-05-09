package com.mybank.security.support;

import java.io.IOException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Outbound HTTP: forwards the incoming JWT from {@link SecurityContextHolder} when present;
 * otherwise uses {@link ClientCredentialsTokenProvider} when configured (service-to-service).
 */
public final class OutboundOAuth2BearerInterceptor implements ClientHttpRequestInterceptor {

    private final ObjectProvider<ClientCredentialsTokenProvider> clientCredentialsProvider;

    public OutboundOAuth2BearerInterceptor(
            ObjectProvider<ClientCredentialsTokenProvider> clientCredentialsProvider
    ) {
        this.clientCredentialsProvider = clientCredentialsProvider;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            request.getHeaders().setBearerAuth(jwtAuth.getToken().getTokenValue());
            return execution.execute(request, body);
        }
        ClientCredentialsTokenProvider provider = clientCredentialsProvider.getIfAvailable();
        if (provider != null) {
            request.getHeaders().setBearerAuth(provider.getAccessToken());
        }
        return execution.execute(request, body);
    }
}
