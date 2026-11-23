package com.zaror.finanza.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zaror.finanza.model.EstadoPago;
import com.zaror.finanza.model.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findAllByOrderByFechaCreacionDesc();

    // Una reserva no se paga dos veces
    boolean existsByReservaIdAndEstado(Long reservaId, EstadoPago estado);

    Optional<Pago> findByWebpayToken(String webpayToken);
}
