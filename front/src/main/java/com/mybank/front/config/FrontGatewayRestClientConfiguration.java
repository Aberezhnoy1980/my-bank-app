package com.mybank.front.config;

import com.mybank.front.security.OAuth2LoginAccessTokenInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;

/**
 * Builds Gateway {@link RestClient} without Spring-injected {@link RestClient.Builder}, so
 * Cloud LoadBalancer post-processors cannot drop OAuth2 Bearer interceptors (see startup WARN
 * around {@code lbRestClientPostProcessor}).
 */
@Configuration
public class FrontGatewayRestClientConfiguration {

    @Bean
    RestClient gatewayRestClient(
            @Value("${app.gateway.base-url}") String baseUrl,
            ObjectProvider<OAuth2AuthorizedClientManager> authorizedClientManagerProvider,
            @Value("${app.oauth2.login-registration-id:keycloak}") String registrationId
    ) {
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        authorizedClientManagerProvider.ifAvailable(manager ->
                builder.requestInterceptor(new OAuth2LoginAccessTokenInterceptor(manager, registrationId)));
        return builder.build();
    }
}
