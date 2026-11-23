package com.zaror.usuario.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity   // habilita @PreAuthorize en los controllers
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final InternalKeyFilter internalKeyFilter;

    public SecurityConfig(JwtFilter jwtFilter, InternalKeyFilter internalKeyFilter) {
        this.jwtFilter = jwtFilter;
        this.internalKeyFilter = internalKeyFilter;
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
                        // Endpoints internos: solo con X-Internal-Key válida
                        .requestMatchers("/usuarios/interno/**").hasRole("INTERNO")
                        // El resto exige JWT; los roles finos van con @PreAuthorize
                        .anyRequest().authenticated())
                .addFilterBefore(internalKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                RespuestaError.escribir(res, HttpStatus.UNAUTHORIZED, "Autenticación requerida"))
                        .accessDeniedHandler((req, res, e) ->
                                RespuestaError.escribir(res, HttpStatus.FORBIDDEN, "No tienes permiso para esta acción")))
                .build();
    }
}
