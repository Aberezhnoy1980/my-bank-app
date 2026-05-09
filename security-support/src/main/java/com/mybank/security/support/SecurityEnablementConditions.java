package com.mybank.security.support;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ConfigurationCondition;

/**
 * Composed {@link Conditional} checks for {@code app.security.*} (repeatable {@link ConditionalOnProperty} workaround).
 */
public final class SecurityEnablementConditions {

    private SecurityEnablementConditions() {
    }

    public static class ResourceServerOutboundEnabled extends AllNestedConditions {

        public ResourceServerOutboundEnabled() {
            super(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true")
        static class OnSecurityEnabled {
        }

        @ConditionalOnProperty(prefix = "app.security", name = "mode", havingValue = "resource-server", matchIfMissing = true)
        static class OnResourceServerMode {
        }
    }

    public static class ClientCredentialsSupportEnabled extends AllNestedConditions {

        public ClientCredentialsSupportEnabled() {
            super(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true")
        static class OnSecurityEnabled {
        }

        @ConditionalOnProperty(prefix = "app.security", name = "mode", havingValue = "resource-server", matchIfMissing = true)
        static class OnResourceServerMode {
        }

        @ConditionalOnProperty(prefix = "app.oauth2", name = "client-registration-id")
        static class OnClientRegistrationId {
        }
    }

    public static class OAuth2LoginOutboundEnabled extends AllNestedConditions {

        public OAuth2LoginOutboundEnabled() {
            super(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true")
        static class OnSecurityEnabled {
        }

        @ConditionalOnProperty(prefix = "app.security", name = "mode", havingValue = "oauth2-login")
        static class OnOAuth2LoginMode {
        }
    }
}
