package com.zaror.finanza.dto;

import java.time.LocalDate;

import com.zaror.finanza.model.EstadoMovimiento;
import com.zaror.finanza.model.TipoMovimiento;

public record MovimientoResponse(
        Long id,
        TipoMovimiento tipo,
        Long categoriaId,
        String categoriaNombre,
        Long reservaId,
        String descripcion,
        int monto,
        LocalDate fechaMovimiento,
        EstadoMovimiento estado,
        Long usuarioRegistroId,
        boolean tieneComprobante) {
}
