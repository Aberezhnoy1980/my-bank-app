package com.mybank.security.support;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * OAuth2 resource server for servlet apps (microservices). Disabled when {@code app.security.enabled=false}.
 * Not loaded for {@code app.security.mode=oauth2-login} (browser UI module uses its own security config).
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(prefix = "app.security", name = "mode", havingValue = "resource-server", matchIfMissing = true)
@EnableWebSecurity
@EnableConfigurationProperties(SecuritySupportProperties.class)
public class ServletOAuth2ResourceServerAutoConfiguration {

    @Bean
    @Order(1)
    SecurityFilterChain servletOAuth2SecurityFilterChain(
            HttpSecurity http,
            SecuritySupportProperties properties
    ) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (!properties.isEnabled()) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
