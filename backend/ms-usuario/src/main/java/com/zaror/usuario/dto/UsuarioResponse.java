package com.zaror.usuario.dto;

import java.time.LocalDate;

import com.zaror.usuario.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        String numeroCasa,
        LocalDate fechaNacimiento,
        boolean recibirAvisos) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getTelefono(),
                u.getNumeroCasa(), u.getFechaNacimiento(), u.isRecibirAvisos());
    }
}
