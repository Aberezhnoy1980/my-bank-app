package com.mybank.front.security;

import com.mybank.security.support.SecurityEnablementConditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

@Configuration
@ConditionalOnBean(OAuth2AuthorizedClientManager.class)
@Conditional(SecurityEnablementConditions.OAuth2LoginOutboundEnabled.class)
public class FrontOAuth2RestClientConfiguration {

    @Bean
    RestClientCustomizer oauth2LoginBearerRestClientCustomizer(
            OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${app.oauth2.login-registration-id:keycloak}") String registrationId
    ) {
        OAuth2LoginAccessTokenInterceptor interceptor =
                new OAuth2LoginAccessTokenInterceptor(authorizedClientManager, registrationId);
        return builder -> builder.requestInterceptors(list -> list.add(interceptor));
    }
}
