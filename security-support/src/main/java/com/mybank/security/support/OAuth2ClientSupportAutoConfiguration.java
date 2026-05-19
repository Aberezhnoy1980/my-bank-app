package com.mybank.security.support;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Registers {@link ClientCredentialsTokenProvider} for {@code client_credentials} when explicitly configured.
 */
@AutoConfiguration
@ConditionalOnClass(OAuth2AuthorizedClientManager.class)
@ConditionalOnBean({OAuth2AuthorizedClientManager.class, ClientRegistrationRepository.class})
@Conditional(SecurityEnablementConditions.ClientCredentialsSupportEnabled.class)
public class OAuth2ClientSupportAutoConfiguration {

    @Bean
    ClientCredentialsTokenProvider clientCredentialsTokenProvider(
            OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${app.oauth2.client-registration-id}") String registrationId
    ) {
        return new ClientCredentialsTokenProvider(authorizedClientManager, registrationId);
    }
}
