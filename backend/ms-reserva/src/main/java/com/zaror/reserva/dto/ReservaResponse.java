package com.zaror.reserva.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zaror.reserva.model.EstadoReserva;
import com.zaror.reserva.model.Reserva;
import com.zaror.reserva.model.Sede;

public record ReservaResponse(
        Long id,
        Long sedeId,
        String sedeNombre,
        String sedeFoto,
        Long usuarioId,
        String usuarioNombre,
        LocalDate fecha,
        @JsonFormat(pattern = "HH:mm") LocalTime horaInicio,
        @JsonFormat(pattern = "HH:mm") LocalTime horaFin,
        String motivo,
        int asistentes,
        EstadoReserva estado,
        boolean pagada,
        int precio,
        String observacion,
        String telefono) {

    // sedeFoto = primera foto de la sede (portada); precio = precio de la sede
    public static ReservaResponse de(Reserva r, Sede s) {
        String foto = s.getFotos().isEmpty() ? null : s.getFotos().get(0);
        return new ReservaResponse(r.getId(), s.getId(), s.getNombre(), foto, r.getUsuarioId(),
                r.getUsuarioNombre(), r.getFecha(), r.getHoraInicio(), r.getHoraFin(), r.getMotivo(),
                r.getAsistentes(), r.getEstado(), r.isPagada(), s.getPrecio(), r.getObservacion(), r.getTelefono());
    }
}
