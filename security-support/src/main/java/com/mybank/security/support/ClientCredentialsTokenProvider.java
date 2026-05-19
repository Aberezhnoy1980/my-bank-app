package com.mybank.security.support;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
/**
 * Fetches access tokens for {@code client_credentials} OAuth2 client registrations
 * (used for service-to-service calls).
 */
public class ClientCredentialsTokenProvider {

    private static final UsernamePasswordAuthenticationToken SERVICE_PRINCIPAL =
            new UsernamePasswordAuthenticationToken(
                    "client-credentials",
                    "N/A",
                    AuthorityUtils.createAuthorityList("ROLE_CLIENT")
            );

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String registrationId;

    public ClientCredentialsTokenProvider(
            OAuth2AuthorizedClientManager authorizedClientManager,
            String registrationId
    ) {
        this.authorizedClientManager = authorizedClientManager;
        this.registrationId = registrationId;
        SERVICE_PRINCIPAL.setAuthenticated(true);
    }

    /**
     * Obtains a bearer token for outbound REST calls (caller thread).
     */
    @NonNull
    public String getAccessToken() {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId(registrationId)
                .principal(resolvePrincipal())
                .build();

        OAuth2AuthorizedClient client = authorizedClientManager.authorize(request);
        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException(
                    "OAuth2 client_credentials authorize failed for registration '" + registrationId + "'"
            );
        }
        return client.getAccessToken().getTokenValue();
    }

    /**
     * Prefer existing authentication context when present (rare for pure client_credentials apps).
     */
    private Authentication resolvePrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth;
        }
        return SERVICE_PRINCIPAL;
    }
}
