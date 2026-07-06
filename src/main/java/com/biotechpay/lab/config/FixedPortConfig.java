package com.biotechpay.lab.config;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FixedPortConfig {

    public static final int PORTUJAVA_FRONTEND_PORT = 62828;

    @Bean
    WebServerFactoryCustomizer<ConfigurableWebServerFactory> portujavaFrontendPortCustomizer() {
        return factory -> factory.setPort(PORTUJAVA_FRONTEND_PORT);
    }
}
