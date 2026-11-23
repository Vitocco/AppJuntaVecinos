package com.zaror.usuario.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Lo envía ms-auth al registrar un vecino (llamada interna)
public record CrearUsuarioRequest(
        @NotNull(message = "El id es obligatorio") Long id,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") String email,
        String telefono,
        String numeroCasa,
        LocalDate fechaNacimiento,
        boolean recibirAvisos) {
}
