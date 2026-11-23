package com.zaror.finanza.dto;

import com.zaror.finanza.model.CategoriaMovimiento;
import com.zaror.finanza.model.TipoMovimiento;

public record CategoriaResponse(Long id, String nombre, TipoMovimiento tipo) {

    public static CategoriaResponse de(CategoriaMovimiento c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getTipo());
    }
}
