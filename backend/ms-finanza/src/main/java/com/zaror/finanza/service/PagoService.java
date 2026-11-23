package com.zaror.finanza.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.zaror.finanza.client.ReservaClient;
import com.zaror.finanza.dto.PagoManualRequest;
import com.zaror.finanza.dto.PagoResponse;
import com.zaror.finanza.dto.ReservaDto;
import com.zaror.finanza.model.EstadoPago;
import com.zaror.finanza.model.MedioPago;
import com.zaror.finanza.model.MovimientoFinanciero;
import com.zaror.finanza.model.Pago;
import com.zaror.finanza.repository.PagoRepository;

import feign.FeignException;

@Service
public class PagoService {

    private final PagoRepository repository;
    private final MovimientoService movimientoService;
    private final ComprobanteService comprobanteService;
    private final ReservaClient reservaClient;

    public PagoService(PagoRepository repository, MovimientoService movimientoService,
                       ComprobanteService comprobanteService, ReservaClient reservaClient) {
        this.repository = repository;
        this.movimientoService = movimientoService;
        this.comprobanteService = comprobanteService;
        this.reservaClient = reservaClient;
    }

    // Lista con datos de la reserva (vecino, sede, fecha) obtenidos de ms-reserva
    public List<PagoResponse> listar() {
        Map<Long, ReservaDto> cache = new HashMap<>();
        return repository.findAllByOrderByFechaCreacionDesc().stream()
                .map(p -> aResponse(p, cache.computeIfAbsent(p.getReservaId(), this::obtenerReservaOpcional)))
                .toList();
    }

    // TESORERO: pago en efectivo/transferencia con foto obligatoria del comprobante
    @Transactional
    public PagoResponse registrarManual(PagoManualRequest req, MultipartFile archivo, Long tesoreroId) {
        if (req.medio() == MedioPago.WEBPAY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los pagos Webpay se registran automáticamente");
        }
        comprobanteService.validar(archivo);
        ReservaDto reserva = validarReservaPagable(req.reservaId());

        // 1) Ingreso "Arriendo sede"  2) Pago PAGADO  3) Comprobante  4) Reserva pagada en ms-reserva
        MovimientoFinanciero mov = movimientoService.crearIngresoArriendo(reserva, req.monto(), req.fechaPago(), tesoreroId);

        Pago p = new Pago();
        p.setReservaId(reserva.id());
        p.setMovimientoId(mov.getId());
        p.setMonto(req.monto());
        p.setFechaPago(req.fechaPago());
        p.setMedio(req.medio());
        p.setReferencia(req.referencia());
        p.setEstado(EstadoPago.PAGADO);
        p.setFechaCreacion(LocalDateTime.now());
        p = repository.save(p);

        comprobanteService.guardar(archivo, mov.getId(), p.getId());

        // Si ms-reserva falla, la excepción hace rollback de todo lo anterior
        reservaClient.marcarPagada(reserva.id());
        return aResponse(p, reserva);
    }

    // ---- Webpay (los usa WebpayService) ----

    @Transactional
    public Pago crearPendienteWebpay(ReservaDto reserva) {
        Pago p = new Pago();
        p.setReservaId(reserva.id());
        p.setMonto(reserva.precio());       // el monto lo define el servidor
        p.setMedio(MedioPago.WEBPAY);
        p.setEstado(EstadoPago.PENDIENTE);
        p.setFechaCreacion(LocalDateTime.now());
        return repository.save(p);
    }

    @Transactional
    public void guardarToken(Long pagoId, String token) {
        buscar(pagoId).setWebpayToken(token);
    }

    // Transbank autorizó: Pago PAGADO + Movimiento INGRESO + reserva pagada
    @Transactional
    public void confirmarWebpay(Long pagoId, String codigoAutorizacion) {
        Pago p = buscar(pagoId);
        ReservaDto reserva = obtenerReserva(p.getReservaId());
        MovimientoFinanciero mov = movimientoService.crearIngresoArriendo(reserva, p.getMonto(), LocalDate.now(), reserva.usuarioId());
        p.setEstado(EstadoPago.PAGADO);
        p.setFechaPago(LocalDate.now());
        p.setReferencia(codigoAutorizacion);
        p.setMovimientoId(mov.getId());
        reservaClient.marcarPagada(reserva.id());
    }

    @Transactional
    public void rechazar(Long pagoId) {
        Pago p = buscar(pagoId);
        if (p.getEstado() == EstadoPago.PENDIENTE) {
            p.setEstado(EstadoPago.RECHAZADO);
        }
    }

    public Pago buscarPorToken(String token) {
        return repository.findByWebpayToken(token).orElse(null);
    }

    public Pago buscarOpcional(Long id) {
        return repository.findById(id).orElse(null);
    }

    // ---- Reglas comunes ----

    // Solo se pagan reservas APROBADAS y nunca dos veces
    public ReservaDto validarReservaPagable(Long reservaId) {
        ReservaDto reserva = obtenerReserva(reservaId);
        if (!"APROBADA".equals(reserva.estado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden pagar reservas aprobadas");
        }
        if (reserva.pagada() || repository.existsByReservaIdAndEstado(reservaId, EstadoPago.PAGADO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva ya está pagada");
        }
        return reserva;
    }

    public ReservaDto obtenerReserva(Long reservaId) {
        try {
            return reservaClient.obtener(reservaId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada");
        }
    }

    private ReservaDto obtenerReservaOpcional(Long reservaId) {
        try {
            return reservaClient.obtener(reservaId);
        } catch (FeignException e) {
            return null;   // el listado igual se muestra, sin nombre/sede
        }
    }

    private Pago buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));
    }

    private PagoResponse aResponse(Pago p, ReservaDto r) {
        return new PagoResponse(p.getId(), p.getReservaId(),
                r != null ? r.usuarioNombre() : null,
                r != null ? r.sedeNombre() : null,
                r != null ? r.fecha() : null,
                p.getMonto(), p.getFechaPago(), p.getMedio(), p.getReferencia(), p.getEstado(), p.getMovimientoId());
    }
}
