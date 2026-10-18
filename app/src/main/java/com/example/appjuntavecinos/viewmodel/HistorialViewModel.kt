package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.AppDatabase
import com.example.appjuntavecinos.data.GastoCategoria
import com.example.appjuntavecinos.model.Informe
import com.example.appjuntavecinos.repository.FinanzasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MesSeleccionado(val anio: Int, val mes: Int)

data class HistorialUiState(
    val mes: MesSeleccionado,
    val ingresos: Int = 0,
    val gastos: Int = 0,
    val saldo: Int = 0,
    val gastosPorCategoria: List<GastoCategoria> = emptyList(),
    val informes: List<Informe> = emptyList(),
    val cargando: Boolean = true
)

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FinanzasRepository(AppDatabase.getInstance(application).finanzasDao())

    val meses = listOf(MesSeleccionado(2026, 9), MesSeleccionado(2026, 10))

    private val _estado = MutableStateFlow(HistorialUiState(mes = meses.last()))
    val estado = _estado.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError = _mensajeError.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repositorio.sembrarSiVacio()
                cargarMes(_estado.value.mes)
            } catch (e: Exception) {
                _mensajeError.value = "No se pudo cargar el historial."
                _estado.update { it.copy(cargando = false) }
            }
        }
    }

    fun seleccionarMes(mes: MesSeleccionado) {
        viewModelScope.launch {
            try {
                _mensajeError.value = null
                cargarMes(mes)
            } catch (e: Exception) {
                _mensajeError.value = "No se pudo cargar el mes seleccionado."
            }
        }
    }

    private suspend fun cargarMes(mes: MesSeleccionado) {
        val resumen = repositorio.resumen(mes.anio, mes.mes)
        val informes = repositorio.informes()
        _estado.update {
            it.copy(
                mes = mes,
                ingresos = resumen.ingresos,
                gastos = resumen.gastos,
                saldo = resumen.saldo,
                gastosPorCategoria = resumen.gastosPorCategoria,
                informes = informes,
                cargando = false
            )
        }
    }
}