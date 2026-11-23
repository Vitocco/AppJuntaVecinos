package com.zaror.finanza.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Foto de la boleta o del comprobante de pago. El archivo vive en disco (app.comprobantes.dir).
@Entity
@Table(name = "comprobante")
@Getter
@Setter
@NoArgsConstructor
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movimiento_id", nullable = false)
    private Long movimientoId;

    @Column(name = "pago_id")
    private Long pagoId;

    // Nombre original del archivo subido
    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    // image/jpeg o image/png
    @Column(name = "tipo_archivo", nullable = false)
    private String tipoArchivo;

    // Nombre con que se guardó dentro de la carpeta de comprobantes
    @Column(name = "ruta_referencia", nullable = false)
    private String rutaReferencia;

    @Column(name = "fecha_adjunto", nullable = false)
    private LocalDateTime fechaAdjunto;
}
