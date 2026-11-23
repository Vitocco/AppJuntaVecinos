package com.zaror.finanza.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.zaror.finanza.dto.InformeDetalleResponse;
import com.zaror.finanza.dto.InformeRequest;
import com.zaror.finanza.dto.InformeResponse;
import com.zaror.finanza.model.InformeFinanciero;
import com.zaror.finanza.model.TipoMovimiento;
import com.zaror.finanza.repository.InformeFinancieroRepository;
import com.zaror.finanza.repository.MovimientoFinancieroRepository;
import com.zaror.finanza.security.UsuarioToken;

@Service
public class InformeService {

    private final InformeFinancieroRepository repository;
    private final MovimientoFinancieroRepository movimientoRepository;

    public InformeService(InformeFinancieroRepository repository, MovimientoFinancieroRepository movimientoRepository) {
        this.repository = repository;
        this.movimientoRepository = movimientoRepository;
    }

    // El VECINO solo ve informes publicados; DIRECTIVA y TESORERO ven todos
    public List<InformeResponse> listar(UsuarioToken usuario) {
        List<InformeFinanciero> informes = esVecino(usuario)
                ? repository.findByPublicadoTrueOrderByPeriodoInicioDesc()
                : repository.findAllByOrderByPeriodoInicioDesc();
        return informes.stream().map(InformeResponse::de).toList();
    }

    public InformeDetalleResponse detalle(Long id, UsuarioToken usuario) {
        InformeFinanciero i = repository.findById(id)
                // Un borrador no existe para el vecino
                .filter(inf -> inf.isPublicado() || !esVecino(usuario))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Informe no encontrado"));
        return conTotales(i);
    }

    @Transactional
    public InformeDetalleResponse crear(InformeRequest req, Long tesoreroId) {
        if (req.periodoFin().isBefore(req.periodoInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El fin del periodo debe ser posterior al inicio");
        }
        InformeFinanciero i = new InformeFinanciero();
        i.setTitulo(req.titulo());
        i.setPeriodoInicio(req.periodoInicio());
        i.setPeriodoFin(req.periodoFin());
        i.setObservacion(req.observacion());
        i.setPublicado(req.publicado());
        i.setFechaGeneracion(LocalDateTime.now());
        i.setUsuarioGeneradorId(tesoreroId);
        return conTotales(repository.save(i));
    }

    // Los totales NO se guardan: se calculan desde los movimientos VIGENTES del periodo
    private InformeDetalleResponse conTotales(InformeFinanciero i) {
        long ingresos = movimientoRepository.sumarPeriodo(TipoMovimiento.INGRESO, i.getPeriodoInicio(), i.getPeriodoFin());
        long gastos = movimientoRepository.sumarPeriodo(TipoMovimiento.GASTO, i.getPeriodoInicio(), i.getPeriodoFin());
        return new InformeDetalleResponse(i.getId(), i.getTitulo(), i.getPeriodoInicio(), i.getPeriodoFin(),
                i.getObservacion(), i.isPublicado(), i.getFechaGeneracion(),
                ingresos, gastos, ingresos - gastos,
                movimientoRepository.gastosPorCategoria(i.getPeriodoInicio(), i.getPeriodoFin()));
    }

    private boolean esVecino(UsuarioToken usuario) {
        return "VECINO".equals(usuario.rol());
    }
}
