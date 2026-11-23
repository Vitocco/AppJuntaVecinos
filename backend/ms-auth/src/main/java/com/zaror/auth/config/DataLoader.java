package com.zaror.auth.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.zaror.auth.model.Credencial;
import com.zaror.auth.model.Rol;
import com.zaror.auth.repository.CredencialRepository;

// Usuarios de prueba (password "1234"). Se crean en este orden para que tengan ids 1..4,
// los mismos que el seed de perfiles en ms-usuario.
@Component
public class DataLoader implements CommandLineRunner {

    private final CredencialRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(CredencialRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        crear("directiva@zaror.cl", Rol.DIRECTIVA);
        crear("tesorero@zaror.cl", Rol.TESORERO);
        crear("camila@zaror.cl", Rol.VECINO);
        crear("luis@zaror.cl", Rol.VECINO);
    }

    private void crear(String email, Rol rol) {
        Credencial c = new Credencial();
        c.setEmail(email);
        c.setPasswordHash(passwordEncoder.encode("1234"));
        c.setRol(rol);
        c.setActivo(true);
        c.setFechaCreacion(LocalDateTime.now());
        repository.save(c);
    }
}
