package com.zaror.reserva.model;

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

// Auditoría: una fila por cada cambio de estado de una reserva (sin endpoint)
@Entity
@Table(name = "historial_reserva")
@Getter
@Setter
@NoArgsConstructor
public class HistorialReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reserva_id", nullable = false)
    private Long reservaId;

    // null cuando la reserva recién se crea
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private EstadoReserva estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoReserva estadoNuevo;

    @Column(name = "usuario_responsable_id", nullable = false)
    private Long usuarioResponsableId;

    private String observacion;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;
}
