package com.zaror.usuario.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zaror.usuario.dto.ActualizarPerfilRequest;
import com.zaror.usuario.dto.CrearUsuarioRequest;
import com.zaror.usuario.dto.UsuarioResponse;
import com.zaror.usuario.security.UsuarioToken;
import com.zaror.usuario.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // El id SIEMPRE sale del token, nunca del body ni de la URL
    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioToken usuario) {
        return service.obtener(usuario.id());
    }

    @PutMapping("/me")
    public UsuarioResponse actualizarMe(@AuthenticationPrincipal UsuarioToken usuario,
                                        @Valid @RequestBody ActualizarPerfilRequest req) {
        return service.actualizar(usuario.id(), req);
    }

    // ---- Internos (X-Internal-Key, los usa ms-auth) ----

    @PostMapping("/interno")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearInterno(@Valid @RequestBody CrearUsuarioRequest req) {
        return service.crear(req);
    }

    @GetMapping("/interno/{id}")
    public UsuarioResponse obtenerInterno(@PathVariable Long id) {
        return service.obtener(id);
    }
}
