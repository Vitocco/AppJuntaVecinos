package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.local.AppDatabase
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.remote.dto.CrearReservaRequest
import com.example.appjuntavecinos.data.remote.dto.OcupacionDto
import com.example.appjuntavecinos.data.remote.dto.SedeDto
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.data.repository.SedeRepository
import com.example.appjuntavecinos.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class SolicitudForm(
    val fecha: LocalDate? = null,
    val desde: String? = null,     // "18:00"
    val hasta: String? = null,     // "23:00"
    val motivo: String = "",
    val personas: String = ""      // String porque viene de un campo de texto
)

data class SolicitudErrores(
    val fecha: String? = null,
    val desde: String? = null,
    val hasta: String? = null,
    val motivo: String? = null,
    val personas: String? = null
)

/** Solicitar arriendo: elegir sede, día, horario, motivo y personas -> POST reservas. */
class SolicitarArriendoViewModel(application: Application) : AndroidViewModel(application) {
    private val sedesRepo = SedeRepository(RetrofitClient.api, AppDatabase.getInstance(application).sedeDao())
    private val reservasRepo = ReservaRepository(RetrofitClient.api, AppDatabase.getInstance(application).reservaCacheDao())

    // Días que se ofrecen: desde mañana, 3 semanas
    val dias: List<LocalDate> = (1..21).map { Fechas.hoy().plusDays(it.toLong()) }

    private val _sedes = MutableStateFlow<EstadoUi<List<SedeDto>>>(EstadoUi.Cargando)
    val sedes: StateFlow<EstadoUi<List<SedeDto>>> = _sedes.asStateFlow()

    private val _sede = MutableStateFlow<SedeDto?>(null)
    val sede: StateFlow<SedeDto?> = _sede.asStateFlow()

    // Horarios ocupados de la sede elegida en los días ofrecidos
    private val _ocupaciones = MutableStateFlow<EstadoUi<List<OcupacionDto>>>(EstadoUi.Cargando)
    val ocupaciones: StateFlow<EstadoUi<List<OcupacionDto>>> = _ocupaciones.asStateFlow()

    private val _form = MutableStateFlow(SolicitudForm())
    val form: StateFlow<SolicitudForm> = _form.asStateFlow()

    private val _errores = MutableStateFlow(SolicitudErrores())
    val errores: StateFlow<SolicitudErrores> = _errores.asStateFlow()

    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()

    // Error del backend (ej. 409 "La sede ya está reservada en ese horario")
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    // true cuando el backend aceptó la solicitud (la pantalla vuelve atrás)
    private val _enviada = MutableStateFlow(false)
    val enviada: StateFlow<Boolean> = _enviada.asStateFlow()

    init { cargarSedes() }

    fun cargarSedes() {
        viewModelScope.launch {
            _sedes.value = EstadoUi.Cargando
            val r = sedesRepo.obtenerSedes()
            _sedes.value = r.aEstadoUi()
            // por defecto, la primera sede activa
            if (r is Resultado.Exito && _sede.value == null) {
                r.datos.firstOrNull { it.activa }?.let { elegirSede(it) }
            }
        }
    }

    fun elegirSede(s: SedeDto) {
        _sede.value = s
        _errores.value = _errores.value.copy(personas = null, desde = null)
        _mensajeError.value = null
        cargarDisponibilidad()
    }

    /** Pide la disponibilidad de cada mes que abarcan los días ofrecidos (pueden ser 2). */
    fun cargarDisponibilidad() {
        val sede = _sede.value ?: return
        viewModelScope.launch {
            _ocupaciones.value = EstadoUi.Cargando
            val meses = dias.map { YearMonth.from(it) }.distinct()
            val todas = mutableListOf<OcupacionDto>()
            for (m in meses) {
                when (val r = sedesRepo.disponibilidad(sede.id, m.year, m.monthValue)) {
                    is Resultado.Exito -> todas += r.datos
                    is Resultado.Error -> { _ocupaciones.value = EstadoUi.Error(r.mensaje); return@launch }
                }
            }
            _ocupaciones.value = EstadoUi.Listo(todas)
        }
    }

    /** Ocupaciones de un día (para pintar la fila de días y avisar los horarios tomados). */
    fun ocupacionesDe(dia: LocalDate): List<OcupacionDto> =
        _ocupaciones.value.datosOrNull.orEmpty().filter { it.fecha == Fechas.aApi(dia) }

    // ---------------- cambios del formulario ----------------
    fun elegirDia(d: LocalDate) {
        _form.value = _form.value.copy(fecha = d)
        _errores.value = _errores.value.copy(fecha = null, desde = null)
        _mensajeError.value = null
    }
    fun elegirDesde(h: String) {
        _form.value = _form.value.copy(desde = h)
        _errores.value = _errores.value.copy(desde = null, hasta = null)
        _mensajeError.value = null
    }
    fun elegirHasta(h: String) {
        _form.value = _form.value.copy(hasta = h)
        _errores.value = _errores.value.copy(desde = null, hasta = null)
        _mensajeError.value = null
    }
    fun actualizarMotivo(v: String) {
        if (v.length <= 100 && v != _form.value.motivo) {
            _form.value = _form.value.copy(motivo = v); _errores.value = _errores.value.copy(motivo = null)
        }
    }
    fun actualizarPersonas(v: String) {
        if (v.all { it.isDigit() } && v.length <= 3 && v != _form.value.personas) {
            _form.value = _form.value.copy(personas = v); _errores.value = _errores.value.copy(personas = null)
        }
    }

    // ---------------- validación (= validarFormulario del profe) ----------------
    private fun validar(): Boolean {
        val f = _form.value
        val capacidad = _sede.value?.capacidad ?: 0
        val personas = f.personas.toIntOrNull()
        // Choque con un horario ya tomado ese día ("HH:mm" se compara bien como texto)
        val choque = if (f.fecha != null && f.desde != null && f.hasta != null) {
            ocupacionesDe(f.fecha).firstOrNull { f.desde < it.horaFin && it.horaInicio < f.hasta }
        } else null

        val e = SolicitudErrores(
            fecha = if (f.fecha == null) "Elige un día" else null,
            desde = when {
                f.desde == null -> "Elige la hora de inicio"
                choque != null -> "Choca con otra reserva (${Fechas.horario(choque.horaInicio, choque.horaFin)})"
                else -> null
            },
            hasta = when {
                f.hasta == null -> "Elige la hora de término"
                f.desde != null && f.hasta <= f.desde -> "Debe ser después del inicio"
                else -> null
            },
            motivo = when {
                f.motivo.isBlank() -> "El motivo es obligatorio"
                f.motivo.any { it.isDigit() } -> "El motivo no puede contener números"
                f.motivo.trim().length < 5 -> "Mínimo 5 caracteres"
                else -> null
            },
            personas = when {
                personas == null -> "Ingresa un número"
                personas !in 1..capacidad -> "Entre 1 y $capacidad personas"
                else -> null
            }
        )
        _errores.value = e
        return e == SolicitudErrores()
    }

    fun enviar() {
        val sede = _sede.value ?: return
        if (_enviando.value || !validar()) return
        val f = _form.value
        viewModelScope.launch {
            _enviando.value = true
            _mensajeError.value = null
            val r = reservasRepo.crear(
                CrearReservaRequest(
                    sedeId = sede.id,
                    fecha = Fechas.aApi(f.fecha!!),
                    horaInicio = f.desde!!,
                    horaFin = f.hasta!!,
                    motivo = f.motivo.trim(),
                    asistentes = f.personas.toInt()
                )
            )
            when (r) {
                is Resultado.Exito -> _enviada.value = true
                is Resultado.Error -> {
                    _mensajeError.value = r.mensaje
                    if (r.codigo == 409) cargarDisponibilidad()   // alguien tomó ese horario: refrescar
                }
            }
            _enviando.value = false
        }
    }
}
