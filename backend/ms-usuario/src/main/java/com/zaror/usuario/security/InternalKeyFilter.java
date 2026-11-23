package com.zaror.usuario.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Rutas /interno/** (llamadas entre microservicios): si el header X-Internal-Key es correcto,
// se autentica como ROLE_INTERNO. No usa JWT.
@Component
public class InternalKeyFilter extends OncePerRequestFilter {

    private final String internalKey;

    public InternalKeyFilter(@Value("${internal.key}") String internalKey) {
        this.internalKey = internalKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().contains("/interno");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (internalKey.equals(request.getHeader("X-Internal-Key"))) {
            var auth = new UsernamePasswordAuthenticationToken("interno", null,
                    List.of(new SimpleGrantedAuthority("ROLE_INTERNO")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
