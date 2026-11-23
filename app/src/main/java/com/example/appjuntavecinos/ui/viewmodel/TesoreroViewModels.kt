package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.local.AppDatabase
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.remote.dto.InformeDto
import com.example.appjuntavecinos.data.remote.dto.MovimientoDto
import com.example.appjuntavecinos.data.remote.dto.PagoDto
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.data.remote.dto.ResumenDto
import com.example.appjuntavecinos.data.repository.FinanzasRepository
import com.example.appjuntavecinos.data.repository.InformeRepository
import com.example.appjuntavecinos.data.repository.PagoRepository
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.data.session.SesionManager
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.ordenCronologico
import com.example.appjuntavecinos.util.porPagar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

// =====================================================================
// Pagos de reservas = pagos ya registrados + reservas APROBADAS sin pagar
// =====================================================================

/** Un elemento de la lista de pagos: o ya está pagado, o la reserva está por pagar. */
sealed interface ItemPago {
    val clave: String
    data class PorPagar(val reserva: ReservaDto) : ItemPago { override val clave = "r${reserva.id}" }
    data class Pagado(val pago: PagoDto) : ItemPago { override val clave = "p${pago.id}" }
}

/**
 * Junta GET pagos y GET reservas?estado=APROBADA. El backend no filtra por "pagada",
 * así que las no pagadas se separan aquí. Primero las por pagar (más próximas primero).
 * De los pagos solo se muestran los PAGADO (un Webpay abandonado queda PENDIENTE/RECHAZADO
 * y su reserva sigue apareciendo como "Por pagar").
 */
fun armarPagos(pagos: List<PagoDto>, aprobadas: List<ReservaDto>): List<ItemPago> =
    aprobadas.filter { it.porPagar }.sortedWith(ordenCronologico).map { ItemPago.PorPagar(it) } +
        pagos.filter { it.estado == "PAGADO" }.sortedByDescending { it.fechaPago }.map { ItemPago.Pagado(it) }

private fun reservasRepo(app: Application) =
    ReservaRepository(RetrofitClient.api, AppDatabase.getInstance(app).reservaCacheDao())

// =====================================================================
// INICIO del tesorero
// =====================================================================
class TesoreroInicioViewModel @JvmOverloads constructor(
    application: Application,
    private val finanzas: FinanzasRepository = FinanzasRepository(RetrofitClient.api),
    private val pagosRepo: PagoRepository = PagoRepository(RetrofitClient.api),
    private val reservas: ReservaRepository = reservasRepo(application)
) : AndroidViewModel(application) {

    val sesion = SesionManager.sesion
    val mes: YearMonth = Fechas.mesActual()

    private val _resumen = MutableStateFlow<EstadoUi<ResumenDto>>(EstadoUi.Cargando)
    val resumen: StateFlow<EstadoUi<ResumenDto>> = _resumen.asStateFlow()

    private val _pagos = MutableStateFlow<EstadoUi<List<ItemPago>>>(EstadoUi.Cargando)
    val pagos: StateFlow<EstadoUi<List<ItemPago>>> = _pagos.asStateFlow()

    private val _movimientos = MutableStateFlow<EstadoUi<List<MovimientoDto>>>(EstadoUi.Cargando)
    val movimientos: StateFlow<EstadoUi<List<MovimientoDto>>> = _movimientos.asStateFlow()

    fun cargar() {
        viewModelScope.launch { _resumen.value = finanzas.resumen(mes.year, mes.monthValue).aEstadoUi() }
        viewModelScope.launch {
            _pagos.value = combinar({ pagosRepo.pagos() }, { reservas.todas("APROBADA") }) { p, r ->
                armarPagos(p, r).take(3)
            }.aEstadoUi()
        }
        viewModelScope.launch {
            val r = finanzas.movimientos(mes.year, mes.monthValue).aEstadoUi()
            _movimientos.value = if (r is EstadoUi.Listo)
                r.copy(datos = r.datos.sortedByDescending { it.fechaMovimiento }.take(3)) else r
        }
    }
}

// =====================================================================
// MOVIMIENTOS (lista por mes, anular, ver comprobante)
// =====================================================================
class MovimientosViewModel(
    private val finanzas: FinanzasRepository = FinanzasRepository(RetrofitClient.api)
) : ViewModel() {

    val meses: List<YearMonth> = Fechas.ultimosMeses(6)

    private val _mes = MutableStateFlow(Fechas.mesActual())
    val mes: StateFlow<YearMonth> = _mes.asStateFlow()

    private val _estado = MutableStateFlow<EstadoUi<List<MovimientoDto>>>(EstadoUi.Cargando)
    val estado: StateFlow<EstadoUi<List<MovimientoDto>>> = _estado.asStateFlow()

    // Movimiento con el menú de acciones abierto (editar / anular / comprobante)
    private val _elegido = MutableStateFlow<MovimientoDto?>(null)
    val elegido: StateFlow<MovimientoDto?> = _elegido.asStateFlow()

    private val _porAnular = MutableStateFlow<MovimientoDto?>(null)
    val porAnular: StateFlow<MovimientoDto?> = _porAnular.asStateFlow()

    private val _anulando = MutableStateFlow(false)
    val anulando: StateFlow<Boolean> = _anulando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun cargar() {
        val m = _mes.value
        viewModelScope.launch {
            val r = finanzas.movimientos(m.year, m.monthValue).aEstadoUi()
            _estado.value = if (r is EstadoUi.Listo) r.copy(datos = r.datos.sortedByDescending { it.fechaMovimiento }) else r
        }
    }

    fun seleccionarMes(nuevo: YearMonth) {
        if (nuevo == _mes.value) return
        _mes.value = nuevo
        _estado.value = EstadoUi.Cargando
        cargar()
    }

    fun abrirAcciones(m: MovimientoDto) { _elegido.value = m }
    fun cerrarAcciones() { _elegido.value = null }

    fun pedirAnular(m: MovimientoDto) { _elegido.value = null; _porAnular.value = m }
    fun descartarAnular() { _porAnular.value = null }

    fun confirmarAnular() {
        val m = _porAnular.value ?: return
        viewModelScope.launch {
            _anulando.value = true
            when (val r = finanzas.anularMovimiento(m.id)) {
                is Resultado.Exito -> { _mensaje.value = "Movimiento anulado"; cargar() }
                is Resultado.Error -> _mensaje.value = r.mensaje   // 409 si ya estaba anulado
            }
            _anulando.value = false
            _porAnular.value = null
        }
    }

    fun mensajeMostrado() { _mensaje.value = null }
}

// =====================================================================
// PAGOS (Todos / Por pagar / Pagados)
// =====================================================================
enum class FiltroPagos(val texto: String) {
    TODOS("Todos"), POR_PAGAR("Por pagar"), PAGADOS("Pagados");

    fun aplicar(i: ItemPago) = when (this) {
        TODOS -> true
        POR_PAGAR -> i is ItemPago.PorPagar
        PAGADOS -> i is ItemPago.Pagado
    }
}

class PagosViewModel @JvmOverloads constructor(
    application: Application,
    private val pagosRepo: PagoRepository = PagoRepository(RetrofitClient.api),
    private val reservas: ReservaRepository = reservasRepo(application)
) : AndroidViewModel(application) {

    private val _filtro = MutableStateFlow(FiltroPagos.TODOS)
    val filtro: StateFlow<FiltroPagos> = _filtro.asStateFlow()

    private val _estado = MutableStateFlow<EstadoUi<List<ItemPago>>>(EstadoUi.Cargando)
    val estado: StateFlow<EstadoUi<List<ItemPago>>> = _estado.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _estado.value = combinar({ pagosRepo.pagos() }, { reservas.todas("APROBADA") }, ::armarPagos).aEstadoUi()
        }
    }

    fun elegirFiltro(f: FiltroPagos) { _filtro.value = f }
}

// =====================================================================
// INFORMES del tesorero (ve también los borradores)
// =====================================================================
class InformesViewModel(
    private val repositorio: InformeRepository = InformeRepository(RetrofitClient.api)
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoUi<List<InformeDto>>>(EstadoUi.Cargando)
    val estado: StateFlow<EstadoUi<List<InformeDto>>> = _estado.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            val r = repositorio.informes().aEstadoUi()
            _estado.value = if (r is EstadoUi.Listo) r.copy(datos = r.datos.sortedByDescending { it.periodoInicio }) else r
        }
    }
}
