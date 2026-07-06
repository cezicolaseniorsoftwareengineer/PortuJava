package com.biotechpay.lab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the embedded web server to {@code server.port}, which application.properties resolves as
 * {@code ${PORT:62828}} - {@value #DEFAULT_PORT} for local dev (the "PortuJava frontend URL
 * invariant"), or whatever port a PaaS host (Railway, Render, ...) injects via the {@code PORT} env
 * var at runtime. A bare {@code server.port} property already achieves this through Spring Boot's
 * own auto-configuration; this bean exists so the local-dev default is a named, tested constant
 * rather than a bare number that could drift out of sync with the property file.
 */
@Configuration
public class FixedPortConfig {

    public static final int DEFAULT_PORT = 62828;

    @Bean
    WebServerFactoryCustomizer<ConfigurableWebServerFactory> portujavaFrontendPortCustomizer(
            @Value("${server.port:" + DEFAULT_PORT + "}") int port) {
        return factory -> factory.setPort(port);
    }
}
