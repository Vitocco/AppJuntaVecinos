package com.zaror.usuario.config;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.zaror.usuario.dto.ErrorResponse;

// Todas las respuestas de error tienen el formato {status, error, mensaje, timestamp}
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Errores de negocio: los services lanzan ResponseStatusException(status, mensaje)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> negocio(ResponseStatusException ex) {
        return respuesta(ex.getStatusCode(), ex.getReason());
    }

    // @Valid fallido -> 400 con el mensaje de cada campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    // JSON mal formado o fecha con formato incorrecto -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es inválido");
    }

    // @PreAuthorize rechazado -> 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> sinPermiso(AccessDeniedException ex) {
        return respuesta(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción");
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatusCode status, String mensaje) {
        String error = HttpStatus.valueOf(status.value()).getReasonPhrase();
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), error, mensaje, LocalDateTime.now()));
    }
}
