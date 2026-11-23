package com.zaror.finanza.dto;

import java.util.List;

// saldo = acumulado al cierre del mes consultado (ingresos - gastos VIGENTES)
public record ResumenResponse(
        long saldo,
        long ingresosMes,
        long gastosMes,
        List<CategoriaTotal> gastosPorCategoria) {
}
