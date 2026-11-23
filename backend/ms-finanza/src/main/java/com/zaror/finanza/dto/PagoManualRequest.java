package com.zaror.finanza.dto;

import java.time.LocalDate;

import com.zaror.finanza.model.MedioPago;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Parte "datos" del multipart en POST /pagos (registro manual del tesorero)
public record PagoManualRequest(
        @NotNull(message = "La reserva es obligatoria") Long reservaId,
        @NotNull(message = "El monto es obligatorio") @Positive(message = "El monto debe ser mayor a 0") Integer monto,
        @NotNull(message = "El medio de pago es obligatorio") MedioPago medio,
        @NotNull(message = "La fecha de pago es obligatoria") LocalDate fechaPago,
        String referencia) {
}
