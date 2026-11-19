package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.local.AppDatabase
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.data.remote.dto.ResumenDto
import com.example.appjuntavecinos.data.repository.FinanzasRepository
import com.example.appjuntavecinos.data.repository.PagoRepository
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.data.session.SesionManager
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.Webpay
import com.example.appjuntavecinos.util.esFutura
import com.example.appjuntavecinos.util.ordenCronologico
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Inicio del vecino: saldo del mes, sus 2 próximas reservas y el pago con Webpay. */
class VecinoInicioViewModel(application: Application) : AndroidViewModel(application) {
    private val finanzas = FinanzasRepository(RetrofitClient.api)
    private val reservasRepo = ReservaRepository(RetrofitClient.api, AppDatabase.getInstance(application).reservaCacheDao())
    private val pagos = PagoRepository(RetrofitClient.api)

    val sesion = SesionManager.sesion
    val mes = Fechas.mesActual()

    private val _resumen = MutableStateFlow<EstadoUi<ResumenDto>>(EstadoUi.Cargando)
    val resumen: StateFlow<EstadoUi<ResumenDto>> = _resumen.asStateFlow()

    // Solo las 2 próximas (hoy o después, PENDIENTE o APROBADA)
    private val _proximas = MutableStateFlow<EstadoUi<List<ReservaDto>>>(EstadoUi.Cargando)
    val proximas: StateFlow<EstadoUi<List<ReservaDto>>> = _proximas.asStateFlow()

    // Id de la reserva cuyo pago se está iniciando (el botón muestra spinner)
    private val _pagando = MutableStateFlow<Long?>(null)
    val pagando: StateFlow<Long?> = _pagando.asStateFlow()

    // URL de Webpay que la pantalla debe abrir (evento de un solo uso)
    private val _abrirUrl = MutableStateFlow<String?>(null)
    val abrirUrl: StateFlow<String?> = _abrirUrl.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _resumen.value = finanzas.resumen(mes.year, mes.monthValue).aEstadoUi()
        }
        viewModelScope.launch {
            val r = reservasRepo.misReservas().aEstadoUi()
            _proximas.value = if (r is EstadoUi.Listo) {
                r.copy(datos = r.datos
                    .filter { it.esFutura && (it.estado == "PENDIENTE" || it.estado == "APROBADA") }
                    .sortedWith(ordenCronologico)
                    .take(2))
            } else r
        }
    }

    /** "Pagar con Webpay": pide la URL al backend y la pantalla abre el Custom Tab. */
    fun pagar(reserva: ReservaDto) {
        if (_pagando.value != null) return
        viewModelScope.launch {
            _pagando.value = reserva.id
            when (val r = pagos.iniciarWebpay(reserva.id)) {
                is Resultado.Exito -> _abrirUrl.value = Webpay.urlFormulario(r.datos)
                is Resultado.Error -> _mensaje.value = r.mensaje
            }
            _pagando.value = null
        }
    }

    fun urlAbierta() { _abrirUrl.value = null }
    fun mensajeMostrado() { _mensaje.value = null }
}
