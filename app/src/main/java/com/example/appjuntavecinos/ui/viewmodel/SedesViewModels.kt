package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.local.AppDatabase
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.remote.dto.OcupacionDto
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.data.remote.dto.SedeDto
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.data.repository.SedeRepository
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.ordenCronologico
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/** Una sede con su estado de hoy: null = libre; "18:00" = ocupada desde esa hora. */
data class SedeHoy(val sede: SedeDto, val ocupadaDesde: String?)

/** Pestaña Calendario (Directiva): tarjetas de sedes con "Libre hoy" / "Ocupada HH:mm". */
class CalendarioViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val sedesRepo = SedeRepository(RetrofitClient.api, db.sedeDao())
    private val reservasRepo = ReservaRepository(RetrofitClient.api, db.reservaCacheDao())

    private val _estado = MutableStateFlow<EstadoUi<List<SedeHoy>>>(EstadoUi.Cargando)
    val estado: StateFlow<EstadoUi<List<SedeHoy>>> = _estado.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            val hoy = Fechas.aApi(Fechas.hoy())
            // sedes + reservas aprobadas, en paralelo
            val r = combinar({ sedesRepo.obtenerSedes() }, { reservasRepo.todas("APROBADA") }) { sedes, aprobadas ->
                sedes.map { sede ->
                    val deHoy = aprobadas.filter { it.sedeId == sede.id && it.fecha == hoy }.minByOrNull { it.horaInicio }
                    SedeHoy(sede, deHoy?.horaInicio)
                }
            }
            _estado.value = r.aEstadoUi()
        }
    }
}

/**
 * Detalle de una sede (Directiva): calendario del mes con los días ocupados
 * (GET sedes/{id}/disponibilidad) y las reservas del día elegido (GET reservas).
 */
class SedeDetalleViewModel(application: Application, estadoGuardado: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val sedesRepo = SedeRepository(RetrofitClient.api, db.sedeDao())
    private val reservasRepo = ReservaRepository(RetrofitClient.api, db.reservaCacheDao())
    private val sedeId: Long = checkNotNull(estadoGuardado.get<Long>("sedeId"))

    private val _sede = MutableStateFlow<EstadoUi<SedeDto>>(EstadoUi.Cargando)
    val sede: StateFlow<EstadoUi<SedeDto>> = _sede.asStateFlow()

    private val _mes = MutableStateFlow(Fechas.mesActual())
    val mes: StateFlow<YearMonth> = _mes.asStateFlow()

    private val _dia = MutableStateFlow(Fechas.hoy())
    val dia: StateFlow<LocalDate> = _dia.asStateFlow()

    // Días ocupados del mes visible (para colorear la grilla)
    private val _ocupaciones = MutableStateFlow<List<OcupacionDto>>(emptyList())
    val ocupaciones: StateFlow<List<OcupacionDto>> = _ocupaciones.asStateFlow()

    // Reservas de esta sede (con nombres) para listar las del día elegido
    private val _reservas = MutableStateFlow<EstadoUi<List<ReservaDto>>>(EstadoUi.Cargando)
    val reservas: StateFlow<EstadoUi<List<ReservaDto>>> = _reservas.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _sede.value = when (val r = sedesRepo.obtenerSedes()) {
                is Resultado.Exito -> r.datos.firstOrNull { it.id == sedeId }
                    ?.let { EstadoUi.Listo(it, r.desdeCache) } ?: EstadoUi.Error("La sede no existe")
                is Resultado.Error -> EstadoUi.Error(r.mensaje)
            }
        }
        viewModelScope.launch {
            val r = reservasRepo.todas().aEstadoUi()
            _reservas.value = if (r is EstadoUi.Listo) {
                r.copy(datos = r.datos.filter {
                    it.sedeId == sedeId && (it.estado == "APROBADA" || it.estado == "PENDIENTE")
                }.sortedWith(ordenCronologico))
            } else r
        }
        cargarMes()
    }

    private fun cargarMes() {
        val m = _mes.value
        viewModelScope.launch {
            val r = sedesRepo.disponibilidad(sedeId, m.year, m.monthValue)
            _ocupaciones.value = (r as? Resultado.Exito)?.datos.orEmpty()
        }
    }

    fun cambiarMes(delta: Long) {
        _mes.value = _mes.value.plusMonths(delta)
        _ocupaciones.value = emptyList()
        cargarMes()
    }

    fun elegirDia(d: LocalDate) { _dia.value = d }
}
