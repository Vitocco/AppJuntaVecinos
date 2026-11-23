package com.zaror.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.zaror.auth.model.Credencial;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

// Solo ms-auth emite tokens. Firma HS256 con el secreto JWT_SECRET (mín. 32 caracteres).
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Claims: sub=email, id, rol, nombre
    public String generarToken(Credencial credencial, String nombre) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(credencial.getEmail())
                .claim("id", credencial.getId())
                .claim("rol", credencial.getRol().name())
                .claim("nombre", nombre)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expirationMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
