package com.zaror.reserva.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zaror.reserva.model.EstadoReserva;
import com.zaror.reserva.model.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioIdOrderByFechaDescHoraInicioDesc(Long usuarioId);

    List<Reserva> findByEstadoOrderByFechaAscHoraInicioAsc(EstadoReserva estado);

    List<Reserva> findAllByOrderByFechaAscHoraInicioAsc();

    // Para el calendario de disponibilidad de una sede en un mes
    List<Reserva> findBySedeIdAndFechaBetweenAndEstadoInOrderByFechaAscHoraInicioAsc(
            Long sedeId, LocalDate desde, LocalDate hasta, Collection<EstadoReserva> estados);

    // ¿Hay otra reserva APROBADA de la misma sede y fecha cuyo horario se cruce?
    // Dos rangos se cruzan si uno empieza antes de que el otro termine y viceversa.
    @Query("""
            select count(r) > 0 from Reserva r
            where r.sedeId = :sedeId and r.fecha = :fecha
              and r.estado = com.zaror.reserva.model.EstadoReserva.APROBADA
              and r.horaInicio < :horaFin and r.horaFin > :horaInicio
              and r.id <> :excluirId
            """)
    boolean existeTopeAprobado(@Param("sedeId") Long sedeId, @Param("fecha") LocalDate fecha,
                               @Param("horaInicio") LocalTime horaInicio, @Param("horaFin") LocalTime horaFin,
                               @Param("excluirId") Long excluirId);
}
