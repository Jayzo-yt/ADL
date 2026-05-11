package com.acd.verify;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServerPortCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        String port = System.getenv("X_ZOHO_CATALYST_LISTEN_PORT");
        if (port == null || port.isBlank()) {
            return;
        }
        try {
            factory.setPort(Integer.parseInt(port.trim()));
        } catch (NumberFormatException ignored) {
            // Ignore invalid env value and keep the configured server.port.
        }
    }
}