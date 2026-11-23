package com.zaror.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Campos que el vecino puede editar de su propio perfil
public record ActualizarPerfilRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El teléfono es obligatorio") String telefono,
        @NotBlank(message = "El número de casa es obligatorio") String numeroCasa,
        @NotNull(message = "Indica si quiere recibir avisos") Boolean recibirAvisos) {
}
