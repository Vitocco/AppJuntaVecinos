package com.zaror.reserva.dto;

import java.util.List;

import com.zaror.reserva.model.Sede;

public record SedeResponse(
        Long id,
        String nombre,
        String descripcion,
        String ubicacion,
        int capacidad,
        int precio,
        boolean activa,
        List<String> fotos) {

    public static SedeResponse de(Sede s) {
        return new SedeResponse(s.getId(), s.getNombre(), s.getDescripcion(), s.getUbicacion(),
                s.getCapacidad(), s.getPrecio(), s.isActiva(), List.copyOf(s.getFotos()));
    }
}
