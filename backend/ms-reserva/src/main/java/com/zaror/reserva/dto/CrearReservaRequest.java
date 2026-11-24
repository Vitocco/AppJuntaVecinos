package com.zaror.reserva.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// fecha "yyyy-MM-dd", horas "HH:mm". El usuario NO viene aquí: sale del token.
public record CrearReservaRequest(
        @NotNull(message = "La sede es obligatoria") Long sedeId,
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
        @NotNull(message = "La hora de inicio es obligatoria") @JsonFormat(pattern = "HH:mm") LocalTime horaInicio,
        @NotNull(message = "La hora de término es obligatoria") @JsonFormat(pattern = "HH:mm") LocalTime horaFin,
        @NotBlank(message = "El motivo es obligatorio") String motivo,
        @NotNull(message = "Los asistentes son obligatorios") @Min(value = 1, message = "Debe haber al menos 1 asistente") Integer asistentes,
        @NotBlank(message = "El teléfono de contacto es obligatorio")
        @Size(min = 9, max = 9, message = "El teléfono debe tener 9 dígitos") String telefono)  {
}
