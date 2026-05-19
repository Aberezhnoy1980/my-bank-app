package com.mybank.security.support;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class SecuritySupportProperties {

    /**
     * When false (default for local CI), requests are permitted and JWT is not validated.
     */
    private boolean enabled = false;

    /**
     * Servlet APIs use {@code resource-server}; the browser UI module uses {@code oauth2-login}.
     */
    private String mode = "resource-server";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
