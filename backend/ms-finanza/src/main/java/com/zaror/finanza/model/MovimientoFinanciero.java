package com.zaror.finanza.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movimiento_financiero")
@Getter
@Setter
@NoArgsConstructor
public class MovimientoFinanciero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    @Column(name = "categoria_id", nullable = false)
    private Long categoriaId;

    // Solo para ingresos por arriendo de sede
    @Column(name = "reserva_id")
    private Long reservaId;

    @Column(nullable = false)
    private String descripcion;

    // Siempre > 0, en CLP
    @Column(nullable = false)
    private int monto;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDate fechaMovimiento;

    @Column(name = "usuario_registro_id", nullable = false)
    private Long usuarioRegistroId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMovimiento estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
