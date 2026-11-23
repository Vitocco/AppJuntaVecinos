package com.zaror.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.zaror.auth.dto.UsuarioDto;

// Llama a los endpoints internos de ms-usuario (el header X-Internal-Key lo agrega FeignConfig)
@FeignClient(name = "ms-usuario", path = "/usuarios/interno")
public interface UsuarioClient {

    @PostMapping
    UsuarioDto crear(@RequestBody UsuarioDto usuario);

    @GetMapping("/{id}")
    UsuarioDto obtener(@PathVariable("id") Long id);
}
