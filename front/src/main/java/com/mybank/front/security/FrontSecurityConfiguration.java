package com.mybank.front.security;

import com.mybank.security.support.SecuritySupportProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

        http.oauth2Login(Customizer.withDefaults());
        http.logout(logout -> logout.logoutSuccessUrl("/"));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
