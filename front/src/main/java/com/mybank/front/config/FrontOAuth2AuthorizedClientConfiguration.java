package com.mybank.front.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

/**
 * Overrides the auto-configured {@link OAuth2AuthorizedClientManager} for browser login so that
 * refresh-token grants use a small clock skew (access tokens expire quickly in Keycloak dev realms).
 */
@Configuration
@Profile("secure")
public class FrontOAuth2AuthorizedClientConfiguration {

    @Bean
    @Primary
    OAuth2AuthorizedClientManager frontOAuth2AuthorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository
    ) {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken(refresh -> refresh.clockSkew(Duration.ofMinutes(1)))
                .build();

        DefaultOAuth2AuthorizedClientManager manager = new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientRepository
        );
        manager.setAuthorizedClientProvider(authorizedClientProvider);
        return manager;
    }
}
