package com.zaror.finanza.dto;

import java.time.LocalDate;

import com.zaror.finanza.model.EstadoPago;
import com.zaror.finanza.model.MedioPago;

// usuarioNombre, sedeNombre y fechaReserva vienen de ms-reserva
public record PagoResponse(
        Long id,
        Long reservaId,
        String usuarioNombre,
        String sedeNombre,
        LocalDate fechaReserva,
        int monto,
        LocalDate fechaPago,
        MedioPago medio,
        String referencia,
        EstadoPago estado,
        Long movimientoId) {
}
