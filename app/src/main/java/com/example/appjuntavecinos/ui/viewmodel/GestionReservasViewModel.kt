package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.local.AppDatabase
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.data.repository.ReservaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Decisión pendiente de confirmar en el diálogo: qué reserva y si se aprueba o rechaza. */
data class Decision(val reserva: ReservaDto, val aprobar: Boolean)

/**
 * Base de las pantallas de la Directiva que aprueban/rechazan solicitudes
 * (Inicio y Solicitudes). Aquí está la lógica común; cada hija define cargar().
 */
abstract class GestionReservasViewModel(application: Application) : AndroidViewModel(application) {
    protected val reservasRepo =
        ReservaRepository(RetrofitClient.api, AppDatabase.getInstance(application).reservaCacheDao())

    private val _porDecidir = MutableStateFlow<Decision?>(null)
    val porDecidir: StateFlow<Decision?> = _porDecidir.asStateFlow()

    private val _decidiendo = MutableStateFlow(false)
    val decidiendo: StateFlow<Boolean> = _decidiendo.asStateFlow()

    protected val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    abstract fun cargar()

    fun aprobar(r: ReservaDto) { _porDecidir.value = Decision(r, aprobar = true) }
    fun rechazar(r: ReservaDto) { _porDecidir.value = Decision(r, aprobar = false) }
    fun descartarDecision() { _porDecidir.value = null }

    /** PUT reservas/{id}/estado con la observación (opcional) que escribió la Directiva. */
    fun confirmarDecision(observacion: String) {
        val d = _porDecidir.value ?: return
        viewModelScope.launch {
            _decidiendo.value = true
            val estado = if (d.aprobar) "APROBADA" else "RECHAZADA"
            when (val r = reservasRepo.cambiarEstado(d.reserva.id, estado, observacion)) {
                is Resultado.Exito -> {
                    _mensaje.value = if (d.aprobar) "Solicitud aprobada" else "Solicitud rechazada"
                    cargar()
                }
                // 409: ya no estaba PENDIENTE o se cruza con otra aprobada
                is Resultado.Error -> _mensaje.value = r.mensaje
            }
            _decidiendo.value = false
            _porDecidir.value = null
        }
    }

    fun mensajeMostrado() { _mensaje.value = null }
}
