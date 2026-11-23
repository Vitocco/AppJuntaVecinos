package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.local.AppDatabase
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.data.repository.PagoRepository
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.util.AbrirWebpay
import com.example.appjuntavecinos.util.Webpay
import com.example.appjuntavecinos.util.esFutura
import com.example.appjuntavecinos.util.ordenCronologico
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Los chips de "Mis reservas". */
enum class FiltroReservas(val texto: String) {
    TODAS("Todas"), PENDIENTES("Pendientes"), APROBADAS("Aprobadas"), PASADAS("Pasadas");

    fun aplicar(r: ReservaDto): Boolean = when (this) {
        TODAS -> true
        PENDIENTES -> r.estado == "PENDIENTE" && r.esFutura
        APROBADAS -> r.estado == "APROBADA" && r.esFutura
        PASADAS -> !r.esFutura
    }
}

/** Pestaña Reservas del vecino: todas sus reservas, filtros, cancelar y pagar. */
class MisReservasViewModel @JvmOverloads constructor(
    application: Application,
    private val repositorio: ReservaRepository =
        ReservaRepository(RetrofitClient.api, AppDatabase.getInstance(application).reservaCacheDao()),
    private val pagos: PagoRepository = PagoRepository(RetrofitClient.api)
) : AndroidViewModel(application) {

    private val _estado = MutableStateFlow<EstadoUi<List<ReservaDto>>>(EstadoUi.Cargando)
    val estado: StateFlow<EstadoUi<List<ReservaDto>>> = _estado.asStateFlow()

    private val _filtro = MutableStateFlow(FiltroReservas.TODAS)
    val filtro: StateFlow<FiltroReservas> = _filtro.asStateFlow()

    // Reserva que espera confirmación para cancelarse (null = sin diálogo)
    private val _porCancelar = MutableStateFlow<ReservaDto?>(null)
    val porCancelar: StateFlow<ReservaDto?> = _porCancelar.asStateFlow()

    private val _cancelando = MutableStateFlow(false)
    val cancelando: StateFlow<Boolean> = _cancelando.asStateFlow()

    private val _pagando = MutableStateFlow<Long?>(null)
    val pagando: StateFlow<Long?> = _pagando.asStateFlow()

    // Reserva cuya hoja "¿Cómo quieres pagar?" está abierta (null = cerrada)
    private val _hojaPago = MutableStateFlow<ReservaDto?>(null)
    val hojaPago: StateFlow<ReservaDto?> = _hojaPago.asStateFlow()

    private val _abrirUrl = MutableStateFlow<AbrirWebpay?>(null)
    val abrirUrl: StateFlow<AbrirWebpay?> = _abrirUrl.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            val r = repositorio.misReservas().aEstadoUi()
            // próximas primero (de la más cercana a la más lejana), luego las pasadas
            _estado.value = if (r is EstadoUi.Listo) r.copy(datos = ordenar(r.datos)) else r
        }
    }

    private fun ordenar(lista: List<ReservaDto>): List<ReservaDto> {
        val (futuras, pasadas) = lista.partition { it.esFutura }
        return futuras.sortedWith(ordenCronologico) + pasadas.sortedWith(ordenCronologico).reversed()
    }

    fun elegirFiltro(f: FiltroReservas) { _filtro.value = f }

    // ---- Cancelar (con confirmación propia) ----
    fun pedirCancelar(r: ReservaDto) { _porCancelar.value = r }
    fun descartarCancelar() { _porCancelar.value = null }

    fun confirmarCancelar() {
        val reserva = _porCancelar.value ?: return
        viewModelScope.launch {
            _cancelando.value = true
            when (val r = repositorio.cancelar(reserva.id)) {
                is Resultado.Exito -> { _mensaje.value = "Reserva cancelada"; cargar() }
                is Resultado.Error -> _mensaje.value = r.mensaje   // ej: 409 "ya está pagada"
            }
            _cancelando.value = false
            _porCancelar.value = null
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
