package com.zaror.finanza.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.zaror.finanza.dto.MovimientoRequest;
import com.zaror.finanza.dto.MovimientoResponse;
import com.zaror.finanza.dto.ReservaDto;
import com.zaror.finanza.model.CategoriaMovimiento;
import com.zaror.finanza.model.EstadoMovimiento;
import com.zaror.finanza.model.MovimientoFinanciero;
import com.zaror.finanza.model.TipoMovimiento;
import com.zaror.finanza.repository.CategoriaMovimientoRepository;
import com.zaror.finanza.repository.MovimientoFinancieroRepository;

@Service
public class MovimientoService {

    private final MovimientoFinancieroRepository repository;
    private final CategoriaMovimientoRepository categoriaRepository;
    private final ComprobanteService comprobanteService;

    public MovimientoService(MovimientoFinancieroRepository repository,
                             CategoriaMovimientoRepository categoriaRepository,
                             ComprobanteService comprobanteService) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.comprobanteService = comprobanteService;
    }

    // Incluye ANULADOS (con su estado) para que la directiva vea todo lo registrado
    public List<MovimientoResponse> listar(int anio, int mes) {
        YearMonth periodo = FinanzaService.periodo(anio, mes);
        Map<Long, String> categorias = categoriaRepository.findAll().stream()
                .collect(Collectors.toMap(CategoriaMovimiento::getId, CategoriaMovimiento::getNombre));
        return repository.findByFechaMovimientoBetweenOrderByFechaMovimientoAscIdAsc(periodo.atDay(1), periodo.atEndOfMonth())
                .stream()
                .map(m -> aResponse(m, categorias.get(m.getCategoriaId())))
                .toList();
    }

    // TESORERO: movimiento con foto de boleta opcional
    @Transactional
    public MovimientoResponse crear(MovimientoRequest req, MultipartFile archivo, Long usuarioId) {
        CategoriaMovimiento categoria = validarCategoria(req.categoriaId(), req.tipo());
        if (archivo != null && !archivo.isEmpty()) {
            comprobanteService.validar(archivo);   // se valida antes de guardar nada
        }

        MovimientoFinanciero m = new MovimientoFinanciero();
        m.setTipo(req.tipo());
        m.setCategoriaId(categoria.getId());
        m.setDescripcion(req.descripcion());
        m.setMonto(req.monto());
        m.setFechaMovimiento(req.fechaMovimiento());
        m.setUsuarioRegistroId(usuarioId);      // del token
        m.setEstado(EstadoMovimiento.VIGENTE);
        m.setFechaCreacion(LocalDateTime.now());
        m = repository.save(m);

        if (archivo != null && !archivo.isEmpty()) {
            comprobanteService.guardar(archivo, m.getId(), null);
        }
        return aResponse(m, categoria.getNombre());
    }

    @Transactional
    public MovimientoResponse editar(Long id, MovimientoRequest req) {
        MovimientoFinanciero m = buscar(id);
        if (m.getEstado() == EstadoMovimiento.ANULADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede editar un movimiento anulado");
        }
        CategoriaMovimiento categoria = validarCategoria(req.categoriaId(), req.tipo());
        m.setTipo(req.tipo());
        m.setCategoriaId(categoria.getId());
        m.setDescripcion(req.descripcion());
        m.setMonto(req.monto());
        m.setFechaMovimiento(req.fechaMovimiento());
        return aResponse(m, categoria.getNombre());
    }

    // DELETE no borra: marca ANULADO y deja de contar en saldos e informes
    @Transactional
    public MovimientoResponse anular(Long id) {
        MovimientoFinanciero m = buscar(id);
        if (m.getEstado() == EstadoMovimiento.ANULADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El movimiento ya está anulado");
        }
        m.setEstado(EstadoMovimiento.ANULADO);
        return aResponse(m, nombreCategoria(m.getCategoriaId()));
    }

    // Ingreso por arriendo de sede (pago manual o Webpay)
    @Transactional
    public MovimientoFinanciero crearIngresoArriendo(ReservaDto reserva, int monto, LocalDate fecha, Long usuarioId) {
        CategoriaMovimiento arriendo = categoriaRepository.findByNombreAndTipo("Arriendo sede", TipoMovimiento.INGRESO)
                .orElseThrow(() -> new IllegalStateException("Falta la categoría 'Arriendo sede'"));
        MovimientoFinanciero m = new MovimientoFinanciero();
        m.setTipo(TipoMovimiento.INGRESO);
        m.setCategoriaId(arriendo.getId());
        m.setReservaId(reserva.id());
        m.setDescripcion("Arriendo " + reserva.sedeNombre() + " - " + reserva.usuarioNombre() + " (" + reserva.fecha() + ")");
        m.setMonto(monto);
        m.setFechaMovimiento(fecha);
        m.setUsuarioRegistroId(usuarioId);
        m.setEstado(EstadoMovimiento.VIGENTE);
        m.setFechaCreacion(LocalDateTime.now());
        return repository.save(m);
    }

    public MovimientoFinanciero buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimiento no encontrado"));
    }

    // La categoría debe existir, estar activa y ser del mismo tipo que el movimiento
    private CategoriaMovimiento validarCategoria(Long categoriaId, TipoMovimiento tipo) {
        CategoriaMovimiento c = categoriaRepository.findById(categoriaId)
                .filter(CategoriaMovimiento::isActiva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría no existe"));
        if (c.getTipo() != tipo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La categoría '" + c.getNombre() + "' no es de tipo " + tipo);
        }
        return c;
    }

    private String nombreCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId).map(CategoriaMovimiento::getNombre).orElse(null);
    }

    private MovimientoResponse aResponse(MovimientoFinanciero m, String categoriaNombre) {
        return new MovimientoResponse(m.getId(), m.getTipo(), m.getCategoriaId(), categoriaNombre, m.getReservaId(),
                m.getDescripcion(), m.getMonto(), m.getFechaMovimiento(), m.getEstado(), m.getUsuarioRegistroId(),
                comprobanteService.tieneComprobante(m.getId()));
    }
}
