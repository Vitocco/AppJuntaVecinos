package com.zaror.auth.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.zaror.auth.client.UsuarioClient;
import com.zaror.auth.dto.AuthResponse;
import com.zaror.auth.dto.LoginRequest;
import com.zaror.auth.dto.RegisterRequest;
import com.zaror.auth.dto.UsuarioDto;
import com.zaror.auth.model.Credencial;
import com.zaror.auth.model.Rol;
import com.zaror.auth.repository.CredencialRepository;
import com.zaror.auth.security.JwtUtil;

@Service
public class AuthService {

    private final CredencialRepository credencialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UsuarioClient usuarioClient;

    public AuthService(CredencialRepository credencialRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, UsuarioClient usuarioClient) {
        this.credencialRepository = credencialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.usuarioClient = usuarioClient;
    }

    // Si la llamada a ms-usuario falla, se hace rollback y no queda una credencial sin perfil
    @Transactional
    public AuthResponse registrar(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (credencialRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        // El registro público SIEMPRE crea un VECINO
        Credencial credencial = new Credencial();
        credencial.setEmail(email);
        credencial.setPasswordHash(passwordEncoder.encode(req.password()));
        credencial.setRol(Rol.VECINO);
        credencial.setActivo(true);
        credencial.setFechaCreacion(LocalDateTime.now());
        credencial = credencialRepository.save(credencial);

        // El perfil en ms-usuario usa el mismo id que la credencial
        usuarioClient.crear(new UsuarioDto(credencial.getId(), req.nombre(), email, req.telefono(),
                req.numeroCasa(), req.fechaNacimiento(), req.recibirAvisos()));

        return respuesta(credencial, req.nombre());
    }

    public AuthResponse login(LoginRequest req) {
        // Mismo mensaje si falla el email o la contraseña (no revela cuál de los dos)
        Credencial credencial = credencialRepository.findByEmail(req.email().trim().toLowerCase())
                .filter(c -> c.isActivo() && passwordEncoder.matches(req.password(), c.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos"));

        String nombre = usuarioClient.obtener(credencial.getId()).nombre();
        return respuesta(credencial, nombre);
    }

    private AuthResponse respuesta(Credencial credencial, String nombre) {
        String token = jwtUtil.generarToken(credencial, nombre);
        return new AuthResponse(token, credencial.getId(), nombre, credencial.getEmail(), credencial.getRol().name());
    }
}
