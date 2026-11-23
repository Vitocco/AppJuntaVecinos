package com.zaror.finanza.model;

import java.time.LocalDate;
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

// Solo guarda el periodo: los totales se CALCULAN siempre desde los movimientos VIGENTES
@Entity
@Table(name = "informe_financiero")
@Getter
@Setter
@NoArgsConstructor
public class InformeFinanciero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    private String observacion;

    @Column(nullable = false)
    private boolean publicado;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "usuario_generador_id", nullable = false)
    private Long usuarioGeneradorId;
}
