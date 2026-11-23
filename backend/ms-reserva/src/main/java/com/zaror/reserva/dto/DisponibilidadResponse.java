package com.zaror.reserva.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zaror.reserva.model.EstadoReserva;

// Horario ocupado de una sede. Sin datos personales del vecino.
public record DisponibilidadResponse(
        LocalDate fecha,
        @JsonFormat(pattern = "HH:mm") LocalTime horaInicio,
        @JsonFormat(pattern = "HH:mm") LocalTime horaFin,
        EstadoReserva estado) {
}
