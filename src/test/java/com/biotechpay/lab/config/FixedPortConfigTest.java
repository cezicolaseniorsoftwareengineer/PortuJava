package com.biotechpay.lab.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;

class FixedPortConfigTest {

    @Test
    void forcesPortuJavaFrontendPort() {
        FixedPortConfig config = new FixedPortConfig();
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setPort(12345);

        config.portujavaFrontendPortCustomizer().customize(factory);

        assertThat(factory.getPort()).isEqualTo(FixedPortConfig.PORTUJAVA_FRONTEND_PORT);
        assertThat(factory.getPort()).isEqualTo(62828);
    }
}
