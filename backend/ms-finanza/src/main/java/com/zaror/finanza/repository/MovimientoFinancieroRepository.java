package com.zaror.finanza.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zaror.finanza.dto.CategoriaTotal;
import com.zaror.finanza.model.MovimientoFinanciero;
import com.zaror.finanza.model.TipoMovimiento;

// Todas las sumas consideran SOLO movimientos VIGENTES (los ANULADOS no cuentan)
public interface MovimientoFinancieroRepository extends JpaRepository<MovimientoFinanciero, Long> {

    List<MovimientoFinanciero> findByFechaMovimientoBetweenOrderByFechaMovimientoAscIdAsc(LocalDate desde, LocalDate hasta);

    // Total de un tipo dentro de un periodo
    @Query("""
            select coalesce(sum(m.monto), 0) from MovimientoFinanciero m
            where m.estado = com.zaror.finanza.model.EstadoMovimiento.VIGENTE
              and m.tipo = :tipo and m.fechaMovimiento between :desde and :hasta
            """)
    long sumarPeriodo(@Param("tipo") TipoMovimiento tipo, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    // Total de un tipo desde el inicio hasta una fecha (para el saldo acumulado)
    @Query("""
            select coalesce(sum(m.monto), 0) from MovimientoFinanciero m
            where m.estado = com.zaror.finanza.model.EstadoMovimiento.VIGENTE
              and m.tipo = :tipo and m.fechaMovimiento <= :hasta
            """)
    long sumarHasta(@Param("tipo") TipoMovimiento tipo, @Param("hasta") LocalDate hasta);

    // Gastos del periodo agrupados por categoría, de mayor a menor
    @Query("""
            select new com.zaror.finanza.dto.CategoriaTotal(c.nombre, sum(m.monto))
            from MovimientoFinanciero m, CategoriaMovimiento c
            where c.id = m.categoriaId
              and m.estado = com.zaror.finanza.model.EstadoMovimiento.VIGENTE
              and m.tipo = com.zaror.finanza.model.TipoMovimiento.GASTO
              and m.fechaMovimiento between :desde and :hasta
            group by c.nombre
            order by sum(m.monto) desc
            """)
    List<CategoriaTotal> gastosPorCategoria(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
