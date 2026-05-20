package com.mybank.front.security;

import com.mybank.security.support.SecuritySupportProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecuritySupportProperties.class)
public class FrontSecurityConfiguration {

    @Bean
    SecurityFilterChain frontSecurityFilterChain(HttpSecurity http, SecuritySupportProperties props) throws Exception {
        if (!props.isEnabled()) {
            http.csrf(csrf -> csrf.disable());
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository =
                new CookieOAuth2AuthorizationRequestRepository();
        http.oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> endpoint
                        .authorizationRequestRepository(authorizationRequestRepository))
                .defaultSuccessUrl("/", true));
        http.oauth2Client(Customizer.withDefaults());
        http.logout(logout -> logout.logoutSuccessUrl("/"));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
