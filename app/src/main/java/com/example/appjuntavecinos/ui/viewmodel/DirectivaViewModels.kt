package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.data.session.SesionManager
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.dia
import com.example.appjuntavecinos.util.esFutura
import com.example.appjuntavecinos.util.ordenCronologico
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

/** Lo que muestra el Inicio de la Directiva, calculado a partir de GET reservas. */
data class InicioDirectiva(
    val pendientes: List<ReservaDto>,     // solicitudes por revisar
    val reservasDelMes: Int,              // aprobadas + pendientes del mes actual
    val proximas: List<ReservaDto>        // próximas aprobadas
)

/** Inicio de la Directiva: contadores, solicitudes por revisar y próximas en la sede. */
class DirectivaInicioViewModel @JvmOverloads constructor(
    application: Application,
    reservasRepo: ReservaRepository = reservasPorDefecto(application)
) : GestionReservasViewModel(application, reservasRepo) {
    val sesion = SesionManager.sesion
    val mes: YearMonth = Fechas.mesActual()

    private val _estado = MutableStateFlow<EstadoUi<InicioDirectiva>>(EstadoUi.Cargando)
    val estado: StateFlow<EstadoUi<InicioDirectiva>> = _estado.asStateFlow()

    override fun cargar() {
        viewModelScope.launch {
            _estado.value = when (val r = reservasRepo.todas()) {
                is Resultado.Exito -> EstadoUi.Listo(calcular(r.datos))
                is Resultado.Error -> EstadoUi.Error(r.mensaje)
            }
        }
    }

    private fun calcular(todas: List<ReservaDto>) = InicioDirectiva(
        pendientes = todas.filter { it.estado == "PENDIENTE" }.sortedWith(ordenCronologico),
        reservasDelMes = todas.count {
            YearMonth.from(it.dia) == mes && (it.estado == "APROBADA" || it.estado == "PENDIENTE")
        },
        proximas = todas.filter { it.estado == "APROBADA" && it.esFutura }.sortedWith(ordenCronologico).take(3)
    )
}

/** Chips de la pestaña Solicitudes: cada uno es un valor de ?estado= (null = todas). */
enum class FiltroSolicitudes(val texto: String, val estado: String?) {
    PENDIENTES("Pendientes", "PENDIENTE"),
    APROBADAS("Aprobadas", "APROBADA"),
    RECHAZADAS("Rechazadas", "RECHAZADA"),
    CANCELADAS("Canceladas", "CANCELADA"),
    TODAS("Todas", null)
}

/** Solicitudes: GET reservas?estado= según el chip elegido. */
class SolicitudesViewModel @JvmOverloads constructor(
    application: Application,
    reservasRepo: ReservaRepository = reservasPorDefecto(application)
) : GestionReservasViewModel(application, reservasRepo) {
    private val _filtro = MutableStateFlow(FiltroSolicitudes.PENDIENTES)
    val filtro: StateFlow<FiltroSolicitudes> = _filtro.asStateFlow()

    private val _estado = MutableStateFlow<EstadoUi<List<ReservaDto>>>(EstadoUi.Cargando)
    val estado: StateFlow<EstadoUi<List<ReservaDto>>> = _estado.asStateFlow()

    fun elegirFiltro(f: FiltroSolicitudes) {
        if (f == _filtro.value) return
        _filtro.value = f
        _estado.value = EstadoUi.Cargando
        cargar()
    }

    override fun cargar() {
        val filtro = _filtro.value
        viewModelScope.launch {
            val r = reservasRepo.todas(filtro.estado).aEstadoUi()
            // pendientes: la más próxima primero; el resto: lo más reciente primero
            _estado.value = if (r is EstadoUi.Listo) {
                r.copy(datos = if (filtro == FiltroSolicitudes.PENDIENTES) r.datos.sortedWith(ordenCronologico)
                else r.datos.sortedWith(ordenCronologico).reversed())
            } else r
        }
    }
}
