package com.mybank.front.security;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

/**
 * Adds Bearer token from the logged-in OAuth2 user session (authorization_code flow).
 */
public final class OAuth2LoginAccessTokenInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginAccessTokenInterceptor.class);

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String registrationId;

    public OAuth2LoginAccessTokenInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager,
            String registrationId
    ) {
        this.authorizedClientManager = authorizedClientManager;
        this.registrationId = registrationId;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof OAuth2AuthenticationToken oauth2Token)) {
            return execution.execute(request, body);
        }
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(registrationId)
                .principal(oauth2Token)
                .build();
        OAuth2AuthorizedClient client;
        try {
            client = authorizedClientManager.authorize(authorizeRequest);
        } catch (OAuth2AuthorizationException ex) {
            log.warn(
                    "OAuth2 authorize failed (often expired refresh / SSO session): {} — {}",
                    ex.getError().getErrorCode(),
                    ex.getError().getDescription()
            );
            throw ex;
        }
        if (client != null && client.getAccessToken() != null) {
            request.getHeaders().setBearerAuth(client.getAccessToken().getTokenValue());
        } else {
            log.warn(
                    "No OAuth2 access token after authorize for registration '{}'; Gateway call may return 401.",
                    registrationId
            );
        }
        return execution.execute(request, body);
    }
}
