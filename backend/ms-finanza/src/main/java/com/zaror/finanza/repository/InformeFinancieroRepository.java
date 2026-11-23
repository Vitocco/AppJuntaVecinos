package com.zaror.finanza.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zaror.finanza.model.InformeFinanciero;

public interface InformeFinancieroRepository extends JpaRepository<InformeFinanciero, Long> {

    List<InformeFinanciero> findAllByOrderByPeriodoInicioDesc();

    List<InformeFinanciero> findByPublicadoTrueOrderByPeriodoInicioDesc();
}
