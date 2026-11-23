package com.zaror.reserva.security;

// Datos del usuario autenticado, sacados del JWT. Se obtiene en el controller con @AuthenticationPrincipal.
public record UsuarioToken(Long id, String email, String rol, String nombre) {
}
