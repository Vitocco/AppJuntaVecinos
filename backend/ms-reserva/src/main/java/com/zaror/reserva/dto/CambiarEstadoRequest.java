package com.zaror.reserva.dto;

import com.zaror.reserva.model.EstadoReserva;

import jakarta.validation.constraints.NotNull;

// La directiva solo puede pasar a APROBADA o RECHAZADA (se valida en el service)
public record CambiarEstadoRequest(
        @NotNull(message = "El estado es obligatorio") EstadoReserva estado,
        String observacion) {
}
