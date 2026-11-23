package com.zaror.finanza.service;

import java.time.YearMonth;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.zaror.finanza.dto.CategoriaResponse;
import com.zaror.finanza.dto.ResumenResponse;
import com.zaror.finanza.model.TipoMovimiento;
import com.zaror.finanza.repository.CategoriaMovimientoRepository;
import com.zaror.finanza.repository.MovimientoFinancieroRepository;

@Service
public class FinanzaService {

    private final MovimientoFinancieroRepository movimientoRepository;
    private final CategoriaMovimientoRepository categoriaRepository;

    public FinanzaService(MovimientoFinancieroRepository movimientoRepository,
                          CategoriaMovimientoRepository categoriaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // Transparencia: cualquier vecino autenticado ve el resumen del mes
    public ResumenResponse resumen(int anio, int mes) {
        YearMonth periodo = periodo(anio, mes);
        var desde = periodo.atDay(1);
        var hasta = periodo.atEndOfMonth();

        // Saldo acumulado hasta el último día del mes (solo VIGENTES)
        long saldo = movimientoRepository.sumarHasta(TipoMovimiento.INGRESO, hasta)
                - movimientoRepository.sumarHasta(TipoMovimiento.GASTO, hasta);

        return new ResumenResponse(
                saldo,
                movimientoRepository.sumarPeriodo(TipoMovimiento.INGRESO, desde, hasta),
                movimientoRepository.sumarPeriodo(TipoMovimiento.GASTO, desde, hasta),
                movimientoRepository.gastosPorCategoria(desde, hasta));
    }

    public List<CategoriaResponse> categorias() {
        return categoriaRepository.findByActivaTrueOrderByTipoAscIdAsc().stream().map(CategoriaResponse::de).toList();
    }

    public static YearMonth periodo(int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mes debe estar entre 1 y 12");
        }
        return YearMonth.of(anio, mes);
    }
}
