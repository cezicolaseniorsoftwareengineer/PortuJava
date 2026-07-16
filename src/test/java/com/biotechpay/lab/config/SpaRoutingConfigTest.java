package com.biotechpay.lab.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpaRoutingConfigTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void forwardsRepositoryLabDeepLinkToAngular() {
        ResponseEntity<String> response = restTemplate.getForEntity("/laboratorio-repositorio", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("<app-root>");
    }
}
