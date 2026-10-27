package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.local.AppDatabase
import com.example.appjuntavecinos.data.model.Reserva
import com.example.appjuntavecinos.data.model.ReservaForm
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReservaErrores(              // = FormularioErrores
    val fecha: String? = null,
    val horaInicio: String? = null,
    val horaTermino: String? = null,
    val motivo: String? = null,
    val asistentes: String? = null
)

class ReservaViewModel(application: Application) : AndroidViewModel(application) {

    companion object { const val MAX_ASISTENTES = 60 }
    private val repositorio =
        ReservaRepository(AppDatabase.getInstance(application).reservaDao())

    private val _form = MutableStateFlow(ReservaForm())
    val form: StateFlow<ReservaForm> = _form.asStateFlow()
    private val _errores = MutableStateFlow(ReservaErrores())
    val errores: StateFlow<ReservaErrores> = _errores.asStateFlow()
    private val _guardadoCorrectamente = MutableStateFlow(false)
    val guardadoCorrectamente: StateFlow<Boolean> = _guardadoCorrectamente.asStateFlow()
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    fun actualizarFecha(v: Long) { _form.value = _form.value.copy(fecha = v) }
    fun actualizarHoraInicio(v: Int) { _form.value = _form.value.copy(horaInicio = v) }
    fun actualizarHoraTermino(v: Int) { _form.value = _form.value.copy(horaTermino = v) }
    fun actualizarMotivo(v: String) { _form.value = _form.value.copy(motivo = v) }
    fun actualizarAsistentes(v: String) {
        if (v.all { it.isDigit() } && v.length <= 3) _form.value = _form.value.copy(asistentes = v)
    }

    // Reglas que NO necesitan la base de datos (= validarFormulario del profesor)
    private fun validarCampos(): ReservaErrores {
        val d = _form.value
        val asistentes = d.asistentes.toIntOrNull()
        return ReservaErrores(
            fecha = if (d.fecha == null) "Selecciona un día" else null,
            horaInicio = if (d.horaInicio == null) "Selecciona la hora de inicio" else null,
            horaTermino = when {
                d.horaTermino == null -> "Selecciona la hora de término"
                d.horaInicio != null && d.horaTermino <= d.horaInicio ->
                    "El término debe ser posterior al inicio"
                else -> null
            },
            motivo = when {
                d.motivo.isBlank() -> "El motivo es obligatorio."
                d.motivo.any { it.isDigit() } -> "El motivo no puede contener números."
                d.motivo.trim().length < 5 -> "Mínimo 5 caracteres"
                else -> null
            },
            asistentes = when {
                asistentes == null -> "Ingresa un número"
                asistentes !in 1..MAX_ASISTENTES -> "Máximo $MAX_ASISTENTES personas"
                else -> null
            }
        )
    }

    fun enviar() {
        val errores = validarCampos()
        _errores.value = errores
        if (errores != ReservaErrores()) return

        val d = _form.value
        val fecha = d.fecha ?: return
        val inicio = d.horaInicio ?: return
        val termino = d.horaTermino ?: return

        viewModelScope.launch {
            try {
                // Regla que SÍ necesita la base de datos: no chocar con otra reserva
                val choques = repositorio.buscarChoques(fecha, inicio, termino)
                if (choques.isNotEmpty()) {
                    val primero = choques.first()
                    val libreDesde = choques.maxOf { it.horaTermino }
                    _errores.value = _errores.value.copy(
                        horaInicio = "Choca con otra reserva " +
                                "(${Fechas.hora(primero.horaInicio)}–${Fechas.hora(primero.horaTermino)}). " +
                                "Elige desde las ${Fechas.hora(libreDesde)}."
                    )
                    return@launch
                }
                repositorio.guardar(
                    Reserva(
                        fecha = fecha, horaInicio = inicio, horaTermino = termino,
                        motivo = d.motivo.trim(), asistentes = d.asistentes.toInt()
                    )
                )
                _guardadoCorrectamente.value = true
            } catch (e: Exception) {
                _mensajeError.value = "No se pudo guardar la solicitud."
            }
        }
    }

    fun consumirGuardado() { _guardadoCorrectamente.value = false }
}