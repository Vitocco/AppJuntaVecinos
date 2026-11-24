package com.zaror.reserva.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.zaror.reserva.dto.CambiarEstadoRequest;
import com.zaror.reserva.dto.CrearReservaRequest;
import com.zaror.reserva.dto.ReservaResponse;
import com.zaror.reserva.model.EstadoReserva;
import com.zaror.reserva.model.HistorialReserva;
import com.zaror.reserva.model.Reserva;
import com.zaror.reserva.model.Sede;
import com.zaror.reserva.repository.HistorialReservaRepository;
import com.zaror.reserva.repository.ReservaRepository;
import com.zaror.reserva.repository.SedeRepository;
import com.zaror.reserva.security.UsuarioToken;

@Service
public class ReservaService {

    private static final String MSG_TOPE = "La sede ya está reservada en ese horario";

    private final ReservaRepository reservaRepository;
    private final HistorialReservaRepository historialRepository;
    private final SedeRepository sedeRepository;
    private final SedeService sedeService;

    public ReservaService(ReservaRepository reservaRepository, HistorialReservaRepository historialRepository,
                          SedeRepository sedeRepository, SedeService sedeService) {
        this.reservaRepository = reservaRepository;
        this.historialRepository = historialRepository;
        this.sedeRepository = sedeRepository;
        this.sedeService = sedeService;
    }

    @Transactional
    public ReservaResponse crear(CrearReservaRequest req, UsuarioToken usuario) {
        Sede sede = sedeService.buscar(req.sedeId());

        // Reglas de negocio de la reserva
        if (!sede.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sede no está disponible");
        }
        if (req.fecha().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha no puede ser anterior a hoy");
        }
        if (!req.horaFin().isAfter(req.horaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de término debe ser posterior a la de inicio");
        }
        if (req.asistentes() > sede.getCapacidad()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los asistentes deben estar entre 1 y " + sede.getCapacidad());
        }
        if (reservaRepository.existeTopeAprobado(sede.getId(), req.fecha(), req.horaInicio(), req.horaFin(), 0L)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, MSG_TOPE);
        }

        Reserva r = new Reserva();
        r.setUsuarioId(usuario.id());          // del token, nunca del body
        r.setUsuarioNombre(usuario.nombre());
        r.setSedeId(sede.getId());
        r.setFecha(req.fecha());
        r.setHoraInicio(req.horaInicio());
        r.setHoraFin(req.horaFin());
        r.setMotivo(req.motivo());
        r.setAsistentes(req.asistentes());
        r.setEstado(EstadoReserva.PENDIENTE);
        r.setPagada(false);
        r.setFechaCreacion(LocalDateTime.now());
        r.setFechaActualizacion(LocalDateTime.now());
        r = reservaRepository.save(r);

        registrarHistorial(r, null, usuario.id(), null);
        return ReservaResponse.de(r, sede);
    }

    public List<ReservaResponse> mias(Long usuarioId) {
        return aResponse(reservaRepository.findByUsuarioIdOrderByFechaDescHoraInicioDesc(usuarioId));
    }

    // Sin estado = todas
    public List<ReservaResponse> listar(EstadoReserva estado) {
        List<Reserva> reservas = estado == null
                ? reservaRepository.findAllByOrderByFechaAscHoraInicioAsc()
                : reservaRepository.findByEstadoOrderByFechaAscHoraInicioAsc(estado);
        return aResponse(reservas);
    }

    // DIRECTIVA: solo PENDIENTE -> APROBADA | RECHAZADA
    @Transactional
    public ReservaResponse cambiarEstado(Long id, CambiarEstadoRequest req, UsuarioToken usuario) {
        if (req.estado() != EstadoReserva.APROBADA && req.estado() != EstadoReserva.RECHAZADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado debe ser APROBADA o RECHAZADA");
        }
        Reserva r = buscar(id);
        if (r.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden aprobar o rechazar reservas pendientes");
        }
        // Al aprobar se vuelve a revisar el tope: otra pudo aprobarse mientras tanto
        if (req.estado() == EstadoReserva.APROBADA
                && reservaRepository.existeTopeAprobado(r.getSedeId(), r.getFecha(), r.getHoraInicio(), r.getHoraFin(), r.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, MSG_TOPE);
        }

        EstadoReserva anterior = r.getEstado();
        r.setEstado(req.estado());
        r.setObservacion(req.observacion());
        r.setFechaActualizacion(LocalDateTime.now());
        registrarHistorial(r, anterior, usuario.id(), req.observacion());
        return ReservaResponse.de(r, sedeService.buscar(r.getSedeId()));
    }

    // Solo el dueño cancela, y solo si no está pagada
    @Transactional
    public ReservaResponse cancelar(Long id, UsuarioToken usuario) {
        Reserva r = buscar(id);
        if (!r.getUsuarioId().equals(usuario.id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño puede cancelar la reserva");
        }
        if (r.isPagada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cancelar una reserva pagada");
        }
        if (r.getEstado() != EstadoReserva.PENDIENTE && r.getEstado() != EstadoReserva.APROBADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva ya está " + r.getEstado());
        }

        EstadoReserva anterior = r.getEstado();
        r.setEstado(EstadoReserva.CANCELADA);
        r.setFechaActualizacion(LocalDateTime.now());
        registrarHistorial(r, anterior, usuario.id(), "Cancelada por el vecino");
        return ReservaResponse.de(r, sedeService.buscar(r.getSedeId()));
    }

    // ---- Internos (los usa ms-finanza) ----

    public ReservaResponse obtener(Long id) {
        Reserva r = buscar(id);
        return ReservaResponse.de(r, sedeService.buscar(r.getSedeId()));
    }

    @Transactional
    public ReservaResponse marcarPagada(Long id) {
        Reserva r = buscar(id);
        if (r.getEstado() != EstadoReserva.APROBADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden pagar reservas aprobadas");
        }
        if (r.isPagada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva ya está pagada");
        }
        r.setPagada(true);
        r.setFechaActualizacion(LocalDateTime.now());
        return ReservaResponse.de(r, sedeService.buscar(r.getSedeId()));
    }

    // ---- Auxiliares ----

    private Reserva buscar(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
    }

    private void registrarHistorial(Reserva r, EstadoReserva anterior, Long responsableId, String observacion) {
        HistorialReserva h = new HistorialReserva();
        h.setReservaId(r.getId());
        h.setEstadoAnterior(anterior);
        h.setEstadoNuevo(r.getEstado());
        h.setUsuarioResponsableId(responsableId);
        h.setObservacion(observacion);
        h.setFechaCambio(LocalDateTime.now());
        historialRepository.save(h);
    }

    // Carga las sedes una sola vez para armar todas las respuestas
    private List<ReservaResponse> aResponse(List<Reserva> reservas) {
        Map<Long, Sede> sedes = sedeRepository.findAll().stream()
                .collect(Collectors.toMap(Sede::getId, Function.identity()));
        return reservas.stream().map(r -> ReservaResponse.de(r, sedes.get(r.getSedeId()))).toList();
    }
}
