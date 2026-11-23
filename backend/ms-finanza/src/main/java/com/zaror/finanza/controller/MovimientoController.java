package com.zaror.finanza.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zaror.finanza.dto.MovimientoRequest;
import com.zaror.finanza.dto.MovimientoResponse;
import com.zaror.finanza.model.Comprobante;
import com.zaror.finanza.security.UsuarioToken;
import com.zaror.finanza.service.ComprobanteService;
import com.zaror.finanza.service.MovimientoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/movimientos")
public class MovimientoController {

    private final MovimientoService service;
    private final ComprobanteService comprobanteService;

    public MovimientoController(MovimientoService service, ComprobanteService comprobanteService) {
        this.service = service;
        this.comprobanteService = comprobanteService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRECTIVA', 'TESORERO')")
    public List<MovimientoResponse> listar(@RequestParam int anio, @RequestParam int mes) {
        return service.listar(anio, mes);
    }

    // multipart: "datos" (JSON) + "archivo" opcional (foto de la boleta)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TESORERO')")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse crear(@Valid @RequestPart("datos") MovimientoRequest datos,
                                    @RequestPart(value = "archivo", required = false) MultipartFile archivo,
                                    @AuthenticationPrincipal UsuarioToken usuario) {
        return service.crear(datos, archivo, usuario.id());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TESORERO')")
    public MovimientoResponse editar(@PathVariable Long id, @Valid @RequestBody MovimientoRequest req) {
        return service.editar(id, req);
    }

    // No borra: lo deja ANULADO
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TESORERO')")
    public MovimientoResponse anular(@PathVariable Long id) {
        return service.anular(id);
    }

    // Devuelve la imagen tal cual (image/jpeg o image/png)
    @GetMapping("/{id}/comprobante")
    @PreAuthorize("hasAnyRole('DIRECTIVA', 'TESORERO')")
    public ResponseEntity<byte[]> comprobante(@PathVariable Long id) {
        service.buscar(id);
        Comprobante c = comprobanteService.buscarDeMovimiento(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(c.getTipoArchivo()))
                .body(comprobanteService.leer(c));
    }
}
