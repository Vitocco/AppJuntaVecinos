package com.zaror.reserva.service;

import java.time.YearMonth;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.zaror.reserva.dto.DisponibilidadResponse;
import com.zaror.reserva.dto.SedeResponse;
import com.zaror.reserva.model.EstadoReserva;
import com.zaror.reserva.model.Sede;
import com.zaror.reserva.repository.ReservaRepository;
import com.zaror.reserva.repository.SedeRepository;

@Service
public class SedeService {

    private final SedeRepository sedeRepository;
    private final ReservaRepository reservaRepository;

    public SedeService(SedeRepository sedeRepository, ReservaRepository reservaRepository) {
        this.sedeRepository = sedeRepository;
        this.reservaRepository = reservaRepository;
    }

    public List<SedeResponse> listarActivas() {
        return sedeRepository.findByActivaTrueOrderByIdAsc().stream().map(SedeResponse::de).toList();
    }

    // Horarios ocupados (PENDIENTE o APROBADA) de la sede en el mes pedido
    public List<DisponibilidadResponse> disponibilidad(Long sedeId, int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mes debe estar entre 1 y 12");
        }
        buscar(sedeId);
        YearMonth periodo = YearMonth.of(anio, mes);
        return reservaRepository.findBySedeIdAndFechaBetweenAndEstadoInOrderByFechaAscHoraInicioAsc(
                        sedeId, periodo.atDay(1), periodo.atEndOfMonth(),
                        List.of(EstadoReserva.PENDIENTE, EstadoReserva.APROBADA))
                .stream()
                .map(r -> new DisponibilidadResponse(r.getFecha(), r.getHoraInicio(), r.getHoraFin(), r.getEstado()))
                .toList();
    }

    public Sede buscar(Long id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sede no encontrada"));
    }
}
