package com.zaror.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zaror.usuario.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
