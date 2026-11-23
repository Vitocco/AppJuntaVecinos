package com.zaror.finanza.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.zaror.finanza.dto.ReservaDto;

// Endpoints internos de ms-reserva (el header X-Internal-Key lo agrega FeignConfig)
@FeignClient(name = "ms-reserva", path = "/reservas/interno")
public interface ReservaClient {

    @GetMapping("/{id}")
    ReservaDto obtener(@PathVariable("id") Long id);

    @PutMapping("/{id}/pagada")
    ReservaDto marcarPagada(@PathVariable("id") Long id);
}
