package com.zaror.reserva.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zaror.reserva.model.Sede;

public interface SedeRepository extends JpaRepository<Sede, Long> {

    List<Sede> findByActivaTrueOrderByIdAsc();
}
