package com.mybank.security.support;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.client.RestClient;

/**
 * Adds outbound Bearer propagation for servlet microservices using {@code RestClient}.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(RestClient.class)
@Conditional(SecurityEnablementConditions.ResourceServerOutboundEnabled.class)
public class OAuth2RestClientSupportAutoConfiguration {

    @Bean
    OutboundOAuth2BearerInterceptor outboundOAuth2BearerInterceptor(
            ObjectProvider<ClientCredentialsTokenProvider> clientCredentialsProvider
    ) {
        return new OutboundOAuth2BearerInterceptor(clientCredentialsProvider);
    }

    @Bean
    RestClientCustomizer oauth2OutboundRestClientCustomizer(OutboundOAuth2BearerInterceptor interceptor) {
        return builder -> builder.requestInterceptors(list -> list.add(interceptor));
    }
}
