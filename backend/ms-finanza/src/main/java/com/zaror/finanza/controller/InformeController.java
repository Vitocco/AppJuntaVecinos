package com.zaror.finanza.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zaror.finanza.dto.InformeDetalleResponse;
import com.zaror.finanza.dto.InformeRequest;
import com.zaror.finanza.dto.InformeResponse;
import com.zaror.finanza.security.UsuarioToken;
import com.zaror.finanza.service.InformeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/informes")
public class InformeController {

    private final InformeService service;

    public InformeController(InformeService service) {
        this.service = service;
    }

    // Autenticado; el VECINO solo recibe los publicados
    @GetMapping
    public List<InformeResponse> listar(@AuthenticationPrincipal UsuarioToken usuario) {
        return service.listar(usuario);
    }

    @GetMapping("/{id}")
    public InformeDetalleResponse detalle(@PathVariable Long id, @AuthenticationPrincipal UsuarioToken usuario) {
        return service.detalle(id, usuario);
    }

    @PostMapping
    @PreAuthorize("hasRole('TESORERO')")
    @ResponseStatus(HttpStatus.CREATED)
    public InformeDetalleResponse crear(@Valid @RequestBody InformeRequest req,
                                        @AuthenticationPrincipal UsuarioToken usuario) {
        return service.crear(req, usuario.id());
    }
}
