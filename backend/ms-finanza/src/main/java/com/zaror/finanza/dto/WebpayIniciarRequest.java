package com.zaror.finanza.dto;

import jakarta.validation.constraints.NotNull;

// Solo la reserva: el monto lo define el servidor (precio de la sede), nunca la app
public record WebpayIniciarRequest(@NotNull(message = "La reserva es obligatoria") Long reservaId) {
}
