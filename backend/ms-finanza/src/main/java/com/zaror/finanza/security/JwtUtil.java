package com.zaror.finanza.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

// Valida tokens emitidos por ms-auth (mismo secreto JWT_SECRET)
@Component
public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Lanza JwtException si la firma no calza o el token expiró
    public UsuarioToken leerToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
        Long id = ((Number) claims.get("id")).longValue();
        return new UsuarioToken(id, claims.getSubject(), claims.get("rol", String.class), claims.get("nombre", String.class));
    }
}
