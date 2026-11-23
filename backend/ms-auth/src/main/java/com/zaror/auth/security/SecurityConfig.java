package com.zaror.auth.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // Sin sesiones: cada petición se valida por sí sola
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ms-auth solo expone login y registro, ambos públicos
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> responder(res, HttpStatus.UNAUTHORIZED, "Autenticación requerida"))
                        .accessDeniedHandler((req, res, e) -> responder(res, HttpStatus.FORBIDDEN, "No tienes permiso para esta acción")))
                .build();
    }

    // BCrypt para guardar y comparar contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Escribe el error con el mismo formato JSON que GlobalExceptionHandler
    private static void responder(HttpServletResponse res, HttpStatus status, String mensaje) throws IOException {
        res.setStatus(status.value());
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(String.format(
                "{\"status\":%d,\"error\":\"%s\",\"mensaje\":\"%s\",\"timestamp\":\"%s\"}",
                status.value(), status.getReasonPhrase(), mensaje, LocalDateTime.now()));
    }
}
