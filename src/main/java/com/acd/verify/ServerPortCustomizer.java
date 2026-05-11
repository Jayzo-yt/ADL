package com.acd.verify;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServerPortCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        String port = System.getenv("X_ZOHO_CATALYST_LISTEN_PORT");
        int listenPort = 9000;
        if (port != null && !port.isBlank()) {
            try {
                listenPort = Integer.parseInt(port.trim());
            } catch (NumberFormatException ignored) {
                // Keep default port if env var is invalid.
            }
        }
        factory.setPort(listenPort);
    }
}