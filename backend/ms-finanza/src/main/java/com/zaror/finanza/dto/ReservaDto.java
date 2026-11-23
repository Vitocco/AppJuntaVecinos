package com.zaror.finanza.dto;

import java.time.LocalDate;

// Lo que ms-finanza necesita de una reserva (respuesta de ms-reserva /reservas/interno/{id})
public record ReservaDto(
        Long id,
        Long sedeId,
        String sedeNombre,
        Long usuarioId,
        String usuarioNombre,
        LocalDate fecha,
        String estado,
        boolean pagada,
        int precio) {
}
