package com.mybank.front.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

/**
 * Замена фрагмента {@code OAuth2WebSecurityConfiguration} из отключённого {@code OAuth2ClientAutoConfiguration}
 * (без второго {@code SecurityFilterChain}).
 */
@Configuration(proxyBeanMethods = false)
@Profile("secure")
public class FrontOAuth2LoginSupportConfiguration {

    @Bean
    OAuth2AuthorizedClientService oauth2AuthorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository
    ) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    OAuth2AuthorizedClientRepository oauth2AuthorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }
}
