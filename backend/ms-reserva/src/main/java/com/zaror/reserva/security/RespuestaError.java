package com.zaror.reserva.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletResponse;

// Errores que ocurren en los filtros (antes del controller), con el mismo JSON que GlobalExceptionHandler
public final class RespuestaError {

    private RespuestaError() {
    }

    public static void escribir(HttpServletResponse res, HttpStatus status, String mensaje) throws IOException {
        res.setStatus(status.value());
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(String.format(
                "{\"status\":%d,\"error\":\"%s\",\"mensaje\":\"%s\",\"timestamp\":\"%s\"}",
                status.value(), status.getReasonPhrase(), mensaje, LocalDateTime.now()));
    }
}
