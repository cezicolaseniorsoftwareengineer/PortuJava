package com.biotechpay.lab.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;

class FixedPortConfigTest {

    @Test
    void defaultsToPortuJavaFrontendPortForLocalDev() {
        FixedPortConfig config = new FixedPortConfig();
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setPort(12345);

        config.portujavaFrontendPortCustomizer(FixedPortConfig.DEFAULT_PORT).customize(factory);

        assertThat(factory.getPort()).isEqualTo(FixedPortConfig.DEFAULT_PORT);
        assertThat(factory.getPort()).isEqualTo(62828);
    }

    @Test
    void honorsAnInjectedPortForCloudHostsLikeRailway() {
        FixedPortConfig config = new FixedPortConfig();
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

        config.portujavaFrontendPortCustomizer(8080).customize(factory);

        assertThat(factory.getPort()).isEqualTo(8080);
    }
}
