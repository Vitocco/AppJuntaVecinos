package com.zaror.finanza.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

@Configuration
public class FeignConfig {

    // Toda llamada Feign lleva la clave interna para que el otro ms la acepte sin JWT
    @Bean
    public RequestInterceptor internalKeyInterceptor(@Value("${internal.key}") String internalKey) {
        return template -> template.header("X-Internal-Key", internalKey);
    }
}
