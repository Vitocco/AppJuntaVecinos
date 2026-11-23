package com.zaror.finanza.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zaror.finanza.model.Comprobante;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

    Optional<Comprobante> findFirstByMovimientoIdOrderByIdDesc(Long movimientoId);

    boolean existsByMovimientoId(Long movimientoId);
}
