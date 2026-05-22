package com.mybank.front.security;

import com.mybank.security.support.SecuritySupportProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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

        http.addFilterBefore(new EnsureHttpSessionFilter(), SecurityContextHolderFilter.class);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        http.oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/", true)
                .failureHandler((request, response, exception) -> response.sendRedirect("/oauth2/authorization/keycloak")));
        http.oauth2Client(Customizer.withDefaults());
        http.logout(logout -> logout.logoutSuccessUrl("/"));
        http.exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                (request, response, authException) -> response.sendRedirect("/oauth2/authorization/keycloak"),
                new AntPathRequestMatcher("/login/oauth2/code/**")
        ));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
