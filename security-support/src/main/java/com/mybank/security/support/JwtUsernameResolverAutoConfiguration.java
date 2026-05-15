package com.mybank.security.support;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class JwtUsernameResolverAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtUsernameResolver jwtUsernameResolver() {
        return new JwtUsernameResolver();
    }
}
