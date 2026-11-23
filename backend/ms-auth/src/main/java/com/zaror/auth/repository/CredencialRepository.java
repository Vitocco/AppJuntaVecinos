package com.zaror.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zaror.auth.model.Credencial;

public interface CredencialRepository extends JpaRepository<Credencial, Long> {

    Optional<Credencial> findByEmail(String email);

    boolean existsByEmail(String email);
}
