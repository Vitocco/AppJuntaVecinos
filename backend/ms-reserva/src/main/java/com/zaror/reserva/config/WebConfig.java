package com.zaror.reserva.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Sirve las imágenes de src/main/resources/static/fotos/ en GET /sedes/fotos/{archivo}
// (vía gateway: /api/sedes/fotos/{archivo})
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/sedes/fotos/**")
                .addResourceLocations("classpath:/static/fotos/");
    }
}
