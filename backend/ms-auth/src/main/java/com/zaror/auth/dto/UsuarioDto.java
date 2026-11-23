package com.zaror.auth.dto;

import java.time.LocalDate;

// Datos que se envían/reciben de ms-usuario por Feign
public record UsuarioDto(
        Long id,
        String nombre,
        String email,
        String telefono,
        String numeroCasa,
        LocalDate fechaNacimiento,
        boolean recibirAvisos) {
}
