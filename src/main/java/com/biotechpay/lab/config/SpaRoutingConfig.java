package com.biotechpay.lab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Angular uses path-based routing, so hard refreshes and shared links reach the server first.
 * Forward every known client route to index.html and let Angular resolve it; API and static asset
 * paths remain untouched.
 */
@Configuration
public class SpaRoutingConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        registry.addViewController("/exercicios/**").setViewName("forward:/index.html");
        registry.addViewController("/laboratorio-repositorio").setViewName("forward:/index.html");
    }
}
