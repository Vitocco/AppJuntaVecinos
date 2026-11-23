package com.zaror.finanza.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Detalle con totales calculados en el momento desde los movimientos VIGENTES del periodo
public record InformeDetalleResponse(
        Long id,
        String titulo,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        String observacion,
        boolean publicado,
        LocalDateTime fechaGeneracion,
        long totalIngresos,
        long totalGastos,
        long saldoPeriodo,
        List<CategoriaTotal> gastosPorCategoria) {
}
