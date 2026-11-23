package com.zaror.finanza.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zaror.finanza.model.CategoriaMovimiento;
import com.zaror.finanza.model.TipoMovimiento;

public interface CategoriaMovimientoRepository extends JpaRepository<CategoriaMovimiento, Long> {

    List<CategoriaMovimiento> findByActivaTrueOrderByTipoAscIdAsc();

    Optional<CategoriaMovimiento> findByNombreAndTipo(String nombre, TipoMovimiento tipo);
}
