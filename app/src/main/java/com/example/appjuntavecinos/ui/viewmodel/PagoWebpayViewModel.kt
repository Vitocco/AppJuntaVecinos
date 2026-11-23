package com.example.appjuntavecinos.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.remote.Resultado
import com.example.appjuntavecinos.data.remote.RetrofitClient
import com.example.appjuntavecinos.data.repository.PagoRepository
import com.example.appjuntavecinos.util.Webpay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Pantalla de pago con Webpay ("webpay?url={url}&reserva={reserva}").
 * [url] es el formulario de la transacción actual. Si el pago vuelve rechazado o anulado,
 * [reintentar] pide una transacción NUEVA para la misma reserva (POST pagos/webpay/iniciar,
 * igual que "Ir a pagar") y [url] cambia, así el WebView carga el formulario nuevo.
 */
class PagoWebpayViewModel @JvmOverloads constructor(
    estadoGuardado: SavedStateHandle,
    private val pagos: PagoRepository = PagoRepository(RetrofitClient.api)
) : ViewModel() {
    private val reservaId: Long = checkNotNull(estadoGuardado.get<Long>("reserva"))

    private val _url = MutableStateFlow(estadoGuardado.get<String>("url").orEmpty())
    val url: StateFlow<String> = _url.asStateFlow()

    private val _reintentando = MutableStateFlow(false)
    val reintentando: StateFlow<Boolean> = _reintentando.asStateFlow()

    // Error al pedir la transacción nueva (ej. sin conexión); null = sin error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun reintentar() {
        if (_reintentando.value) return
        viewModelScope.launch {
            _reintentando.value = true
            _error.value = null
            when (val r = pagos.iniciarWebpay(reservaId)) {
                is Resultado.Exito -> _url.value = Webpay.urlFormulario(r.datos)
                is Resultado.Error -> _error.value = r.mensaje
            }
            _reintentando.value = false
        }
    }
}
