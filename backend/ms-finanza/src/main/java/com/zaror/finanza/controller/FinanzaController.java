package com.zaror.finanza.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zaror.finanza.dto.CategoriaResponse;
import com.zaror.finanza.dto.ResumenResponse;
import com.zaror.finanza.service.FinanzaService;

@RestController
public class FinanzaController {

    private final FinanzaService service;

    public FinanzaController(FinanzaService service) {
        this.service = service;
    }

    // Cualquier usuario autenticado (transparencia para los vecinos)
    @GetMapping("/finanzas/resumen")
    public ResumenResponse resumen(@RequestParam int anio, @RequestParam int mes) {
        return service.resumen(anio, mes);
    }

    @GetMapping("/categorias")
    @PreAuthorize("hasRole('TESORERO')")
    public List<CategoriaResponse> categorias() {
        return service.categorias();
    }
}
