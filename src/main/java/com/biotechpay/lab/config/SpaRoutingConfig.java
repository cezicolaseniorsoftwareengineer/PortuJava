package com.biotechpay.lab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Angular uses path-based routing, so a hard refresh or a shared link on /exercicios/... reaches the
 * server directly and used to 404 (Whitelabel). Forward the client-side routes to index.html and let
 * the Angular router resolve them; API and static asset paths are untouched.
 */
@Configuration
public class SpaRoutingConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/exercicios/**").setViewName("forward:/index.html");
    }
}
