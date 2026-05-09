package com.mybank.security.support;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * JWT validation for Spring Cloud Gateway (reactive).
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(SecurityWebFilterChain.class)
@EnableConfigurationProperties(SecuritySupportProperties.class)
@EnableWebFluxSecurity
public class ReactiveOAuth2ResourceServerAutoConfiguration {

    @Bean
    SecurityWebFilterChain reactiveOAuth2SecurityWebFilterChain(
            ServerHttpSecurity http,
            SecuritySupportProperties properties
    ) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        if (!properties.isEnabled()) {
            http.authorizeExchange(ex -> ex.anyExchange().permitAll());
            return http.build();
        }
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        http.authorizeExchange(ex -> ex
                .pathMatchers("/actuator/**").permitAll()
                .anyExchange().authenticated());
        return http.build();
    }
}
