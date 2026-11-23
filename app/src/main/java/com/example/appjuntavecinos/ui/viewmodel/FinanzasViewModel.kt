package com.example.appjuntavecinos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.remote.dto.InformeDto
import com.example.appjuntavecinos.data.remote.dto.MovimientoDto
import com.example.appjuntavecinos.data.remote.dto.ResumenDto
import com.example.appjuntavecinos.data.repository.FinanzasRepository
import com.example.appjuntavecinos.data.repository.InformeRepository
import com.example.appjuntavecinos.data.session.SesionManager
import com.example.appjuntavecinos.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

/**
 * Finanzas (Vecino y Directiva): resumen del mes elegido, gastos por categoría,
 * informes publicados y — solo Directiva/Tesorero — la lista de movimientos.
 */
class FinanzasViewModel(
    private val finanzas: FinanzasRepository = FinanzasRepository(RetrofitClient.api),
    private val informesRepo: InformeRepository = InformeRepository(RetrofitClient.api)
) : ViewModel() {

    // El vecino no tiene permiso para GET movimientos (403): solo se piden si el rol puede
    val verMovimientos: Boolean = SesionManager.sesion.value?.rol in setOf("DIRECTIVA", "TESORERO")

    val meses: List<YearMonth> = Fechas.ultimosMeses(6)

    private val _mes = MutableStateFlow(Fechas.mesActual())
    val mes: StateFlow<YearMonth> = _mes.asStateFlow()

    private val _resumen = MutableStateFlow<EstadoUi<ResumenDto>>(EstadoUi.Cargando)
    val resumen: StateFlow<EstadoUi<ResumenDto>> = _resumen.asStateFlow()

    private val _movimientos = MutableStateFlow<EstadoUi<List<MovimientoDto>>>(EstadoUi.Cargando)
    val movimientos: StateFlow<EstadoUi<List<MovimientoDto>>> = _movimientos.asStateFlow()

    private val _informes = MutableStateFlow<EstadoUi<List<InformeDto>>>(EstadoUi.Cargando)
    val informes: StateFlow<EstadoUi<List<InformeDto>>> = _informes.asStateFlow()

    /** Se llama al entrar a la pantalla y al "deslizar para actualizar". */
    fun cargar() {
        cargarMes()
        viewModelScope.launch {
            val r = informesRepo.informes().aEstadoUi()
            // más nuevos primero
            _informes.value = if (r is EstadoUi.Listo) r.copy(datos = r.datos.sortedByDescending { it.periodoInicio }) else r
        }
    }

    fun seleccionarMes(nuevo: YearMonth) {
        if (nuevo == _mes.value) return
        _mes.value = nuevo
        _resumen.value = EstadoUi.Cargando
        _movimientos.value = EstadoUi.Cargando
        cargarMes()
    }

    private fun cargarMes() {
        val m = _mes.value
        viewModelScope.launch {
            _resumen.value = finanzas.resumen(m.year, m.monthValue).aEstadoUi()
        }
        if (verMovimientos) viewModelScope.launch {
            val r = finanzas.movimientos(m.year, m.monthValue).aEstadoUi()
            _movimientos.value = if (r is EstadoUi.Listo) r.copy(datos = r.datos.sortedByDescending { it.fechaMovimiento }) else r
        }
    }
}
