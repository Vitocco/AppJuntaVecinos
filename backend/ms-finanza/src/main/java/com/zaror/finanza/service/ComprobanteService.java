package com.zaror.finanza.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.zaror.finanza.model.Comprobante;
import com.zaror.finanza.repository.ComprobanteRepository;

// Guarda las fotos de boletas/comprobantes en disco y su referencia en la BD
@Service
public class ComprobanteService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png");
    private static final long MAX_BYTES = 5 * 1024 * 1024;   // 5 MB

    private final ComprobanteRepository repository;
    private final Path carpeta;

    public ComprobanteService(ComprobanteRepository repository, @Value("${app.comprobantes.dir}") String dir) {
        this.repository = repository;
        this.carpeta = Paths.get(dir).toAbsolutePath().normalize();
    }

    // Solo JPG/PNG de hasta 5 MB
    public void validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes adjuntar la foto del comprobante");
        }
        if (!TIPOS_PERMITIDOS.contains(archivo.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se aceptan imágenes JPG o PNG");
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo supera el máximo de 5 MB");
        }
    }

    public Comprobante guardar(MultipartFile archivo, Long movimientoId, Long pagoId) {
        validar(archivo);
        // Nombre aleatorio: evita choques y que el usuario controle la ruta
        String extension = "image/png".equals(archivo.getContentType()) ? ".png" : ".jpg";
        String nombreEnDisco = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(carpeta);
            archivo.transferTo(carpeta.resolve(nombreEnDisco));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el comprobante", e);
        }

        Comprobante c = new Comprobante();
        c.setMovimientoId(movimientoId);
        c.setPagoId(pagoId);
        c.setNombreArchivo(archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : nombreEnDisco);
        c.setTipoArchivo(archivo.getContentType());
        c.setRutaReferencia(nombreEnDisco);
        c.setFechaAdjunto(LocalDateTime.now());
        return repository.save(c);
    }

    public boolean tieneComprobante(Long movimientoId) {
        return repository.existsByMovimientoId(movimientoId);
    }

    public Comprobante buscarDeMovimiento(Long movimientoId) {
        return repository.findFirstByMovimientoIdOrderByIdDesc(movimientoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El movimiento no tiene comprobante"));
    }

    public byte[] leer(Comprobante c) {
        try {
            return Files.readAllBytes(carpeta.resolve(c.getRutaReferencia()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo del comprobante no encontrado");
        }
    }
}
