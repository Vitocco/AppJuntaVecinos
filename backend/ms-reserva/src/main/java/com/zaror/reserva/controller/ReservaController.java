package com.zaror.reserva.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zaror.reserva.dto.CambiarEstadoRequest;
import com.zaror.reserva.dto.CrearReservaRequest;
import com.zaror.reserva.dto.ReservaResponse;
import com.zaror.reserva.model.EstadoReserva;
import com.zaror.reserva.security.UsuarioToken;
import com.zaror.reserva.service.ReservaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    // Cualquier usuario autenticado; el dueño sale del token
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crear(@Valid @RequestBody CrearReservaRequest req,
                                 @AuthenticationPrincipal UsuarioToken usuario) {
        return service.crear(req, usuario);
    }

    @GetMapping("/mias")
    public List<ReservaResponse> mias(@AuthenticationPrincipal UsuarioToken usuario) {
        return service.mias(usuario.id());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRECTIVA', 'TESORERO')")
    public List<ReservaResponse> listar(@RequestParam(required = false) EstadoReserva estado) {
        return service.listar(estado);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('DIRECTIVA')")
    public ReservaResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest req,
                                         @AuthenticationPrincipal UsuarioToken usuario) {
        return service.cambiarEstado(id, req, usuario);
    }

    // No borra: pasa a CANCELADA (el service valida que sea el dueño)
    @DeleteMapping("/{id}")
    public ReservaResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal UsuarioToken usuario) {
        return service.cancelar(id, usuario);
    }

    // ---- Internos (X-Internal-Key, los usa ms-finanza) ----

    @GetMapping("/interno/{id}")
    public ReservaResponse obtenerInterno(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/interno/{id}/pagada")
    public ReservaResponse marcarPagada(@PathVariable Long id) {
        return service.marcarPagada(id);
    }
}
