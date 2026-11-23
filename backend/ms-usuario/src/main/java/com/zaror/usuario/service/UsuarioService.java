package com.zaror.usuario.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.zaror.usuario.dto.ActualizarPerfilRequest;
import com.zaror.usuario.dto.CrearUsuarioRequest;
import com.zaror.usuario.dto.UsuarioResponse;
import com.zaror.usuario.model.Usuario;
import com.zaror.usuario.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.de(buscar(id));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarPerfilRequest req) {
        Usuario u = buscar(id);
        u.setNombre(req.nombre());
        u.setTelefono(req.telefono());
        u.setNumeroCasa(req.numeroCasa());
        u.setRecibirAvisos(req.recibirAvisos());
        return UsuarioResponse.de(u);
    }

    // Llamado por ms-auth al registrar: el id viene de la credencial
    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest req) {
        if (repository.existsById(req.id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese id");
        }
        Usuario u = new Usuario();
        u.setId(req.id());
        u.setNombre(req.nombre());
        u.setEmail(req.email());
        u.setTelefono(req.telefono());
        u.setNumeroCasa(req.numeroCasa());
        u.setFechaNacimiento(req.fechaNacimiento());
        u.setRecibirAvisos(req.recibirAvisos());
        u.setFechaCreacion(LocalDateTime.now());
        return UsuarioResponse.de(repository.save(u));
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}
