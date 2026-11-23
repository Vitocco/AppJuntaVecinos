package com.zaror.finanza.config;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.zaror.finanza.dto.ErrorResponse;

import feign.FeignException;

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

    // Falta un parámetro obligatorio (?anio, ?mes) -> 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> parametroFaltante(MissingServletRequestParameterException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Falta el parámetro " + ex.getParameterName());
    }

    // Parámetro con tipo o valor inválido (?anio=abc, ?estado=XYZ) -> 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Valor inválido para " + ex.getName());
    }

    // Foto que no existe en static/fotos -> 404
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> recursoNoEncontrado(NoResourceFoundException ex) {
        return respuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado");
    }

    // ---- Multipart (movimientos y pagos con foto) ----

    // Falta "datos" o "archivo" -> 400
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> parteFaltante(MissingServletRequestPartException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Falta la parte '" + ex.getRequestPartName() + "'");
    }

    // La parte "datos" debe ir con Content-Type application/json -> 400
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> tipoNoSoportado(HttpMediaTypeNotSupportedException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Formato no soportado: 'datos' debe enviarse como application/json");
    }

    // Archivo sobre el límite (spring.servlet.multipart.max-file-size) -> 400
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> archivoGrande(MaxUploadSizeExceededException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "El archivo supera el máximo de 5 MB");
    }

    // Falla al llamar a ms-reserva -> 503
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> feign(FeignException ex) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, "Error al comunicarse con otro servicio");
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
