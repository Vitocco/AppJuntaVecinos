package com.zaror.auth.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

// fechaNacimiento llega como "yyyy-MM-dd"
public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") String email,
        @NotBlank(message = "El teléfono es obligatorio") String telefono,
        @NotBlank(message = "El número de casa es obligatorio") String numeroCasa,
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser pasada") LocalDate fechaNacimiento,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres") String password,
        @NotNull(message = "Indica si quiere recibir avisos") Boolean recibirAvisos) {
}
