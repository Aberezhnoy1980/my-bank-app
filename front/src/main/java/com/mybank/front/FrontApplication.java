package com.mybank.front;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;

/**
 * {@link OAuth2ClientAutoConfiguration} импортирует {@code OAuth2ClientRegistrationRepositoryConfiguration}
 * через {@code @Import} — исключение только последнего через {@code spring.autoconfigure.exclude} не срабатывает.
 * Регистрация клиента Keycloak задаётся вручную ({@code FrontKeycloakClientRegistrationConfiguration}).
 */
@SpringBootApplication(exclude = {OAuth2ClientAutoConfiguration.class})
public class FrontApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontApplication.class, args);
    }
}
