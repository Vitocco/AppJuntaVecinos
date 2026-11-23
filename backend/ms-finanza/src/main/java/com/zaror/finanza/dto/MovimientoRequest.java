package com.zaror.finanza.dto;

import java.time.LocalDate;

import com.zaror.finanza.model.TipoMovimiento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Parte "datos" del multipart en POST /movimientos, y body JSON en PUT /movimientos/{id}
public record MovimientoRequest(
        @NotNull(message = "El tipo es obligatorio") TipoMovimiento tipo,
        @NotNull(message = "La categoría es obligatoria") Long categoriaId,
        @NotBlank(message = "La descripción es obligatoria") String descripcion,
        @NotNull(message = "El monto es obligatorio") @Positive(message = "El monto debe ser mayor a 0") Integer monto,
        @NotNull(message = "La fecha es obligatoria") LocalDate fechaMovimiento) {
}
