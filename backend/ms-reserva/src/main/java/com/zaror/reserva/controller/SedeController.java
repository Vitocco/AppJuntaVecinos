package com.zaror.reserva.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zaror.reserva.dto.DisponibilidadResponse;
import com.zaror.reserva.dto.SedeResponse;
import com.zaror.reserva.service.SedeService;

// Las fotos (GET /sedes/fotos/{archivo}) no pasan por aquí: las sirve WebConfig como archivos estáticos
@RestController
@RequestMapping("/sedes")
public class SedeController {

    private final SedeService service;

    public SedeController(SedeService service) {
        this.service = service;
    }

    @GetMapping
    public List<SedeResponse> listar() {
        return service.listarActivas();
    }

    @GetMapping("/{id}/disponibilidad")
    public List<DisponibilidadResponse> disponibilidad(@PathVariable Long id,
                                                       @RequestParam int anio,
                                                       @RequestParam int mes) {
        return service.disponibilidad(id, anio, mes);
    }
}
