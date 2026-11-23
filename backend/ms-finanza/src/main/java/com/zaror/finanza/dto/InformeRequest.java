package com.zaror.finanza.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InformeRequest(
        @NotBlank(message = "El título es obligatorio") String titulo,
        @NotNull(message = "El inicio del periodo es obligatorio") LocalDate periodoInicio,
        @NotNull(message = "El fin del periodo es obligatorio") LocalDate periodoFin,
        String observacion,
        @NotNull(message = "Indica si el informe queda publicado") Boolean publicado) {
}
