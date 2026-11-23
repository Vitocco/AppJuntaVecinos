package com.zaror.finanza.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.zaror.finanza.model.InformeFinanciero;

// Para el listado (sin totales)
public record InformeResponse(
        Long id,
        String titulo,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        String observacion,
        boolean publicado,
        LocalDateTime fechaGeneracion) {

    public static InformeResponse de(InformeFinanciero i) {
        return new InformeResponse(i.getId(), i.getTitulo(), i.getPeriodoInicio(), i.getPeriodoFin(),
                i.getObservacion(), i.isPublicado(), i.getFechaGeneracion());
    }
}
