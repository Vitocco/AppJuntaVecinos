package com.example.appjuntavecinos.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.local.AppDatabase
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.remote.dto.CategoriaDto
import com.example.appjuntavecinos.data.remote.dto.CrearInformeRequest
import com.example.appjuntavecinos.data.remote.dto.MovimientoRequest
import com.example.appjuntavecinos.data.remote.dto.PagoRequest
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.data.repository.FinanzasRepository
import com.example.appjuntavecinos.data.repository.InformeRepository
import com.example.appjuntavecinos.data.repository.PagoRepository
import com.example.appjuntavecinos.data.repository.ReservaRepository
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.Imagenes
import com.example.appjuntavecinos.util.porPagar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

// =====================================================================
// REGISTRAR / EDITAR MOVIMIENTO
// =====================================================================

data class MovimientoForm(
    val tipo: String = "INGRESO",          // INGRESO | GASTO
    val categoriaId: Long? = null,
    val monto: String = "",                // String porque viene de un TextField
    val fecha: LocalDate = Fechas.hoy(),
    val detalle: String = "",
    val comprobante: Uri? = null           // foto opcional (solo al crear)
)

data class MovimientoErrores(
    val categoria: String? = null,
    val monto: String? = null,
    val fecha: String? = null,
    val detalle: String? = null
)

/**
 * Parámetros de la ruta: tipo (INGRESO/GASTO), camara (abrir la cámara al entrar)
 * y, para EDITAR, id + fecha (con la fecha se busca el movimiento en su mes).
 */
class RegistrarMovimientoViewModel @JvmOverloads constructor(
    application: Application,
    estadoGuardado: SavedStateHandle,
    private val finanzas: FinanzasRepository = FinanzasRepository(RetrofitClient.api)
) : AndroidViewModel(application) {

    private val idEditar: Long? = estadoGuardado.get<Long>("id")?.takeIf { it > 0 }
    private val fechaEditar: String? = estadoGuardado.get<String>("fecha")
    val esEdicion: Boolean = idEditar != null
    val abrirCamara: Boolean = estadoGuardado.get<Boolean>("camara") ?: false

    private val _categorias = MutableStateFlow<EstadoUi<List<CategoriaDto>>>(EstadoUi.Cargando)
    val categorias: StateFlow<EstadoUi<List<CategoriaDto>>> = _categorias.asStateFlow()

    private val _form = MutableStateFlow(MovimientoForm(tipo = estadoGuardado.get<String>("tipo") ?: "INGRESO"))
    val form: StateFlow<MovimientoForm> = _form.asStateFlow()

    private val _errores = MutableStateFlow(MovimientoErrores())
    val errores: StateFlow<MovimientoErrores> = _errores.asStateFlow()

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private val _guardado = MutableStateFlow(false)
    val guardado: StateFlow<Boolean> = _guardado.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _categorias.value = EstadoUi.Cargando
            val cats = finanzas.categorias()
            if (cats is Resultado.Error) { _categorias.value = EstadoUi.Error(cats.mensaje); return@launch }
            // Edición: buscar el movimiento en su mes y llenar el formulario
            if (idEditar != null && fechaEditar != null) {
                val f = Fechas.leer(fechaEditar)
                when (val r = finanzas.movimientos(f.year, f.monthValue)) {
                    is Resultado.Exito -> r.datos.firstOrNull { it.id == idEditar }?.let { m ->
                        _form.value = MovimientoForm(m.tipo, m.categoriaId, m.monto.toString(),
                            Fechas.leer(m.fechaMovimiento), m.descripcion.orEmpty())
                    }
                    is Resultado.Error -> { _categorias.value = EstadoUi.Error(r.mensaje); return@launch }
                }
            }
            _categorias.value = cats.aEstadoUi()
        }
    }

    // Cada cambio actualiza el formulario y limpia el error de ESE campo
    fun actualizarTipo(tipo: String) {
        // al cambiar de tipo, la categoría elegida deja de servir (son de otro tipo)
        _form.value = _form.value.copy(tipo = tipo, categoriaId = null)
        _errores.value = _errores.value.copy(categoria = null)
    }
    fun actualizarCategoria(id: Long) {
        _form.value = _form.value.copy(categoriaId = id); _errores.value = _errores.value.copy(categoria = null)
    }
    fun actualizarMonto(v: String) {
        if (v.all { it.isDigit() } && v.length <= 9 && v != _form.value.monto) {
            _form.value = _form.value.copy(monto = v); _errores.value = _errores.value.copy(monto = null)
        }
    }
    fun actualizarFecha(f: LocalDate) {
        _form.value = _form.value.copy(fecha = f); _errores.value = _errores.value.copy(fecha = null)
    }
    fun actualizarDetalle(v: String) {
        if (v.length <= 60 && v != _form.value.detalle) {
            _form.value = _form.value.copy(detalle = v); _errores.value = _errores.value.copy(detalle = null)
        }
    }
    fun actualizarComprobante(uri: Uri?) { _form.value = _form.value.copy(comprobante = uri) }

    // Reglas de validación (= validarFormulario del profesor)
    private fun validar(): Boolean {
        val d = _form.value
        val monto = d.monto.toLongOrNull()
        val e = MovimientoErrores(
            categoria = if (d.categoriaId == null) "Selecciona una categoría" else null,
            monto = if (monto == null || monto <= 0) "El monto debe ser mayor a \$0" else null,
            fecha = if (d.fecha.isAfter(Fechas.hoy())) "La fecha no puede ser futura" else null,
            detalle = when {
                d.detalle.isBlank() -> "El detalle es obligatorio"
                d.detalle.trim().length < 3 -> "Mínimo 3 caracteres"
                else -> null
            }
        )
        _errores.value = e
        return e == MovimientoErrores()
    }

    fun guardar() {
        if (_guardando.value || !validar()) return
        val d = _form.value
        val datos = MovimientoRequest(d.tipo, d.categoriaId!!, d.detalle.trim(), d.monto.toLong(), Fechas.aApi(d.fecha))
        viewModelScope.launch {
            _guardando.value = true
            _mensajeError.value = null
            val r = if (idEditar != null) {
                finanzas.editarMovimiento(idEditar, datos)                // PUT (JSON)
            } else {
                // La foto se comprime a JPG antes de subirla (multipart, parte "archivo")
                val jpg = d.comprobante?.let {
                    runCatching { Imagenes.comprimirAJpg(getApplication(), it) }.getOrNull()
                }
                finanzas.crearMovimiento(datos, jpg)                      // POST multipart
            }
            when (r) {
                is Resultado.Exito -> _guardado.value = true
                is Resultado.Error -> _mensajeError.value = r.mensaje
            }
            _guardando.value = false
        }
    }
}

// =====================================================================
// REGISTRAR PAGO de una reserva (tesorero)
// =====================================================================

/** Medios que puede registrar el tesorero (Webpay solo lo usa el vecino). */
enum class MedioPago(val texto: String) { TRANSFERENCIA("Transferencia"), EFECTIVO("Efectivo"), OTRO("Otro") }

data class PagoForm(
    val monto: String = "",
    val medio: MedioPago = MedioPago.TRANSFERENCIA,
    val fecha: LocalDate = Fechas.hoy(),
    val referencia: String = "",
    val comprobante: Uri? = null           // OBLIGATORIO
)

data class PagoErrores(
    val monto: String? = null,
    val fecha: String? = null,
    val referencia: String? = null,
    val comprobante: String? = null
)

class RegistrarPagoViewModel @JvmOverloads constructor(
    application: Application,
    estadoGuardado: SavedStateHandle,
    private val reservasRepo: ReservaRepository =
        ReservaRepository(RetrofitClient.api, AppDatabase.getInstance(application).reservaCacheDao()),
    private val pagos: PagoRepository = PagoRepository(RetrofitClient.api)
) : AndroidViewModel(application) {
    private val reservaId: Long = checkNotNull(estadoGuardado.get<Long>("reservaId"))

    private val _reserva = MutableStateFlow<EstadoUi<ReservaDto>>(EstadoUi.Cargando)
    val reserva: StateFlow<EstadoUi<ReservaDto>> = _reserva.asStateFlow()

    private val _form = MutableStateFlow(PagoForm())
    val form: StateFlow<PagoForm> = _form.asStateFlow()

    private val _errores = MutableStateFlow(PagoErrores())
    val errores: StateFlow<PagoErrores> = _errores.asStateFlow()

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private val _guardado = MutableStateFlow(false)
    val guardado: StateFlow<Boolean> = _guardado.asStateFlow()

    init { cargar() }

    /** Busca la reserva entre las APROBADAS; el monto parte con su precio. */
    fun cargar() {
        viewModelScope.launch {
            _reserva.value = EstadoUi.Cargando
            _reserva.value = when (val r = reservasRepo.todas("APROBADA")) {
                is Resultado.Exito -> {
                    val encontrada = r.datos.firstOrNull { it.id == reservaId && it.porPagar }
                    if (encontrada != null) {
                        if (_form.value.monto.isEmpty()) _form.value = _form.value.copy(monto = encontrada.precio.toString())
                        EstadoUi.Listo(encontrada)
                    } else EstadoUi.Error("La reserva ya no está pendiente de pago")
                }
                is Resultado.Error -> EstadoUi.Error(r.mensaje)
            }
        }
    }

    fun actualizarMonto(v: String) {
        if (v.all { it.isDigit() } && v.length <= 9 && v != _form.value.monto) {
            _form.value = _form.value.copy(monto = v); _errores.value = _errores.value.copy(monto = null)
        }
    }
    fun actualizarMedio(m: MedioPago) { _form.value = _form.value.copy(medio = m) }
    fun actualizarFecha(f: LocalDate) {
        _form.value = _form.value.copy(fecha = f); _errores.value = _errores.value.copy(fecha = null)
    }
    fun actualizarReferencia(v: String) {
        if (v.length <= 40 && v != _form.value.referencia) {
            _form.value = _form.value.copy(referencia = v); _errores.value = _errores.value.copy(referencia = null)
        }
    }
    fun actualizarComprobante(uri: Uri?) {
        _form.value = _form.value.copy(comprobante = uri); _errores.value = _errores.value.copy(comprobante = null)
    }

    private fun validar(): Boolean {
        val d = _form.value
        val monto = d.monto.toLongOrNull()
        val e = PagoErrores(
            monto = if (monto == null || monto <= 0) "El monto debe ser mayor a \$0" else null,
            fecha = if (d.fecha.isAfter(Fechas.hoy())) "La fecha no puede ser futura" else null,
            referencia = when {
                d.referencia.isBlank() -> "Ingresa el N° de transferencia o de recibo"
                d.referencia.trim().length < 3 -> "Mínimo 3 caracteres"
                else -> null
            },
            comprobante = if (d.comprobante == null) "Adjunta la foto del comprobante" else null
        )
        _errores.value = e
        return e == PagoErrores()
    }

    fun guardar() {
        if (_guardando.value || !validar()) return
        val d = _form.value
        viewModelScope.launch {
            _guardando.value = true
            _mensajeError.value = null
            val jpg = runCatching { Imagenes.comprimirAJpg(getApplication(), d.comprobante!!) }.getOrNull()
            if (jpg == null) {
                _errores.value = _errores.value.copy(comprobante = "No se pudo leer la foto, elige otra")
                _guardando.value = false
                return@launch
            }
            val r = pagos.registrarPago(
                PagoRequest(reservaId, d.monto.toLong(), d.medio.name, Fechas.aApi(d.fecha), d.referencia.trim()),
                jpg
            )
            when (r) {
                is Resultado.Exito -> _guardado.value = true
                is Resultado.Error -> _mensajeError.value = r.mensaje    // 409: ya pagada / no aprobada
            }
            _guardando.value = false
        }
    }
}

// =====================================================================
// NUEVO INFORME
// =====================================================================

data class InformeForm(
    val titulo: String = "",
    val inicio: LocalDate? = Fechas.mesActual().atDay(1),
    val fin: LocalDate? = Fechas.mesActual().atEndOfMonth(),
    val observacion: String = "",
    val publicado: Boolean = false
)

data class InformeErrores(val titulo: String? = null, val inicio: String? = null, val fin: String? = null)

class NuevoInformeViewModel(
    private val repositorio: InformeRepository = InformeRepository(RetrofitClient.api)
) : ViewModel() {

    private val _form = MutableStateFlow(InformeForm(titulo = "Rendición ${Fechas.nombreMes(Fechas.mesActual()).lowercase()}"))
    val form: StateFlow<InformeForm> = _form.asStateFlow()

    private val _errores = MutableStateFlow(InformeErrores())
    val errores: StateFlow<InformeErrores> = _errores.asStateFlow()

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private val _guardado = MutableStateFlow(false)
    val guardado: StateFlow<Boolean> = _guardado.asStateFlow()

    fun actualizarTitulo(v: String) {
        if (v.length <= 60 && v != _form.value.titulo) {
            _form.value = _form.value.copy(titulo = v); _errores.value = _errores.value.copy(titulo = null)
        }
    }
    fun actualizarInicio(f: LocalDate) { _form.value = _form.value.copy(inicio = f); _errores.value = InformeErrores(titulo = _errores.value.titulo) }
    fun actualizarFin(f: LocalDate) { _form.value = _form.value.copy(fin = f); _errores.value = InformeErrores(titulo = _errores.value.titulo) }
    fun actualizarObservacion(v: String) { if (v.length <= 300 && v != _form.value.observacion) _form.value = _form.value.copy(observacion = v) }
    fun actualizarPublicado(v: Boolean) { _form.value = _form.value.copy(publicado = v) }

    private fun validar(): Boolean {
        val d = _form.value
        val e = InformeErrores(
            titulo = when {
                d.titulo.isBlank() -> "El título es obligatorio"
                d.titulo.trim().length < 5 -> "Mínimo 5 caracteres"
                else -> null
            },
            inicio = if (d.inicio == null) "Elige el inicio del período" else null,
            fin = when {
                d.fin == null -> "Elige el fin del período"
                d.inicio != null && d.fin.isBefore(d.inicio) -> "Debe ser igual o posterior al inicio"
                else -> null
            }
        )
        _errores.value = e
        return e == InformeErrores()
    }

    fun guardar() {
        if (_guardando.value || !validar()) return
        val d = _form.value
        viewModelScope.launch {
            _guardando.value = true
            _mensajeError.value = null
            val r = repositorio.crear(
                CrearInformeRequest(d.titulo.trim(), Fechas.aApi(d.inicio!!), Fechas.aApi(d.fin!!), d.observacion.trim(), d.publicado)
            )
            when (r) {
                is Resultado.Exito -> _guardado.value = true
                is Resultado.Error -> _mensajeError.value = r.mensaje
            }
            _guardando.value = false
        }
    }
}
