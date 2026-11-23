package com.zaror.finanza.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// ms-finanza no expone endpoints /interno (solo los consume), por eso no lleva InternalKeyFilter
@Configuration
@EnableMethodSecurity   // habilita @PreAuthorize en los controllers
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        // Transbank redirige aquí el navegador del vecino, sin token
                        .requestMatchers(HttpMethod.GET, "/pagos/webpay/retorno").permitAll()
                        .requestMatchers(HttpMethod.POST, "/pagos/webpay/retorno").permitAll()
                        // El resto exige JWT; los roles finos van con @PreAuthorize
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                RespuestaError.escribir(res, HttpStatus.UNAUTHORIZED, "Autenticación requerida"))
                        .accessDeniedHandler((req, res, e) ->
                                RespuestaError.escribir(res, HttpStatus.FORBIDDEN, "No tienes permiso para esta acción")))
                .build();
    }
}
