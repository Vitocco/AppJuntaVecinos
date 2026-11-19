package com.example.appjuntavecinos.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.remote.dto.InformeDto
import com.example.appjuntavecinos.data.repository.InformeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Detalle de un informe (GET informes/{id}).
 * El id llega por la ruta ("informe/{id}"): Navigation lo guarda en el SavedStateHandle.
 */
class InformeDetalleViewModel(estadoGuardado: SavedStateHandle) : ViewModel() {
    private val repositorio = InformeRepository(RetrofitClient.api)
    private val id: Long = checkNotNull(estadoGuardado.get<Long>("id"))

    private val _estado = MutableStateFlow<EstadoUi<InformeDto>>(EstadoUi.Cargando)
    val estado: StateFlow<EstadoUi<InformeDto>> = _estado.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = EstadoUi.Cargando
            _estado.value = repositorio.detalle(id).aEstadoUi()
        }
    }
}
