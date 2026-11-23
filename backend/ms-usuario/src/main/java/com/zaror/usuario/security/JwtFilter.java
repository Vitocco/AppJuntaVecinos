package com.zaror.usuario.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Si viene "Authorization: Bearer <token>", lo valida y deja al usuario autenticado con authority ROLE_<rol>
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            try {
                UsuarioToken usuario = jwtUtil.leerToken(header.substring(7));
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol()));
                var auth = new UsernamePasswordAuthenticationToken(usuario, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException | NullPointerException e) {
                // Token inválido o expirado -> 401 inmediato
                RespuestaError.escribir(response, HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
                return;
            }
        }
        // Sin token: sigue; SecurityConfig decide si la ruta lo requiere
        chain.doFilter(request, response);
    }
}
