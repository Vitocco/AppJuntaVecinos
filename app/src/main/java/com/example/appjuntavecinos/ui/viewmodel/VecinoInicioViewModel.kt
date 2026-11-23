package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.local.AppDatabase
import com.example.appjuntavecinos.data.remote.FeriadosClient
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.remote.dto.Feriado
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.data.remote.dto.ResumenDto
import com.example.appjuntavecinos.data.repository.FeriadoRepository
import com.example.appjuntavecinos.data.repository.FinanzasRepository
import com.example.appjuntavecinos.data.repository.PagoRepository
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.data.session.SesionManager
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.AbrirWebpay
import com.example.appjuntavecinos.util.Webpay
import com.example.appjuntavecinos.util.esFutura
import com.example.appjuntavecinos.util.ordenCronologico
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Inicio del vecino: saldo del mes, sus 2 próximas reservas, el pago de reservas
 * y los próximos feriados.
 * Los repositorios se pueden pasar por constructor (pruebas con MockK); @JvmOverloads deja
 * también el constructor (Application) que usa viewModel().
 */
class VecinoInicioViewModel @JvmOverloads constructor(
    application: Application,
    private val feriadosRepo: FeriadoRepository = FeriadoRepository(FeriadosClient.api),
    private val finanzas: FinanzasRepository = FinanzasRepository(RetrofitClient.api),
    private val reservasRepo: ReservaRepository =
        ReservaRepository(RetrofitClient.api, AppDatabase.getInstance(application).reservaCacheDao()),
    private val pagos: PagoRepository = PagoRepository(RetrofitClient.api)
) : AndroidViewModel(application) {

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
    // Reserva cuya hoja "¿Cómo quieres pagar?" está abierta (null = cerrada)
    private val _hojaPago = MutableStateFlow<ReservaDto?>(null)
    val hojaPago: StateFlow<ReservaDto?> = _hojaPago.asStateFlow()

    private val _abrirUrl = MutableStateFlow<AbrirWebpay?>(null)
    val abrirUrl: StateFlow<AbrirWebpay?> = _abrirUrl.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // Próximos 5 feriados de Chile (Nager.Date). Vacía si no hay internet: la tarjeta no se muestra.
    private val _feriados = MutableStateFlow<List<Feriado>>(emptyList())
    val feriados: StateFlow<List<Feriado>> = _feriados.asStateFlow()

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

    /** Feriados del año actual y del siguiente, desde hoy en adelante, los 5 primeros. */
    fun cargarFeriados() {
        viewModelScope.launch {
            val hoy = Fechas.hoy()
            val actual = async { feriadosRepo.obtenerFeriados(hoy.year) }
            val siguiente = async { feriadosRepo.obtenerFeriados(hoy.year + 1) }
            val todos = listOf(actual.await(), siguiente.await())
                .flatMap { (it as? Resultado.Exito)?.datos.orEmpty() }
            _feriados.value = todos
                .filter { f -> runCatching { !Fechas.leer(f.date).isBefore(hoy) }.getOrDefault(false) }
                .sortedBy { it.date }
                .take(5)
        }
    }

    // ---- Pagar ----
    /** Botón "Pagar $X": abre la hoja con los medios de pago. */
    fun abrirHojaPago(reserva: ReservaDto) { _hojaPago.value = reserva }
    fun cerrarHojaPago() { _hojaPago.value = null }

    /** Webpay > "Ir a pagar": pide la URL al backend y la pantalla abre PagoWebpayScreen. */
    fun pagarConWebpay() {
        val reserva = _hojaPago.value ?: return
        if (_pagando.value != null) return
        viewModelScope.launch {
            _pagando.value = reserva.id
            when (val r = pagos.iniciarWebpay(reserva.id)) {
                is Resultado.Exito -> {
                    _hojaPago.value = null
                    _abrirUrl.value = AbrirWebpay(reserva.id, Webpay.urlFormulario(r.datos))
                }
                is Resultado.Error -> _mensaje.value = r.mensaje
            }
            _pagando.value = null
        }
    }

    fun urlAbierta() { _abrirUrl.value = null }
    fun mensajeMostrado() { _mensaje.value = null }
}
