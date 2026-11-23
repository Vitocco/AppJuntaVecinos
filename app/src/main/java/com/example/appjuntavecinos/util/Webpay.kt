package com.example.appjuntavecinos.util

import android.net.Uri
import com.example.appjuntavecinos.data.remote.dto.WebpayIniciarResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Flujo Webpay (Etapa 4):
 *  1. La app pide al backend POST pagos/webpay/iniciar -> {url, token}
 *  2. PagoWebpayScreen abre en un WebView  url?token_ws=token  (formulario de Transbank)
 *  3. Transbank vuelve al backend, que confirma el pago y redirige a zaror://pago?estado=ok|error
 *  4. El WebView intercepta ese enlace (o, si se abriera fuera, MainActivity) -> EventosPago avisa
 *     y las pantallas recargan reservas/mias y ven "pagada: true".
 */
object Webpay {

    fun urlFormulario(r: WebpayIniciarResponse): String =
        Uri.parse(r.url).buildUpon().appendQueryParameter("token_ws", r.token).build().toString()
}

/**
 * Resultado que llega por el deep link zaror://pago?estado=ok|error.
 * PagoWebpayScreen (o MainActivity) lo publica aquí; el Scaffold del vecino lo muestra en un Snackbar.
 */
object EventosPago {
    private val _resultado = MutableStateFlow<Boolean?>(null)   // true = aprobado, false = rechazado
    val resultado: StateFlow<Boolean?> = _resultado.asStateFlow()

    fun recibir(uri: Uri?) {
        if (uri?.scheme == "zaror" && uri.host == "pago") {
            _resultado.value = uri.getQueryParameter("estado") == "ok"
        }
    }

    fun consumir() { _resultado.value = null }
}
