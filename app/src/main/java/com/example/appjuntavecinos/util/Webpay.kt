package com.example.appjuntavecinos.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.graphics.toArgb
import com.example.appjuntavecinos.data.remote.dto.WebpayIniciarResponse
import com.example.appjuntavecinos.ui.theme.Carbon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Flujo Webpay (Etapa 4):
 *  1. La app pide al backend POST pagos/webpay/iniciar -> {url, token}
 *  2. Se abre un Custom Tab en  url?token_ws=token  (formulario de Transbank)
 *  3. Transbank vuelve al backend, que confirma el pago y redirige a zaror://pago?estado=ok|error
 *  4. Android abre la app con ese deep link (MainActivity) -> EventosPago avisa a las pantallas,
 *     que recargan reservas/mias y ven "pagada: true".
 */
object Webpay {

    fun urlFormulario(r: WebpayIniciarResponse): String =
        Uri.parse(r.url).buildUpon().appendQueryParameter("token_ws", r.token).build().toString()

    /** Abre el formulario de pago en un Custom Tab con la barra del color de la app. */
    fun abrir(context: Context, url: String) {
        val colores = CustomTabColorSchemeParams.Builder().setToolbarColor(Carbon.toArgb()).build()
        CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colores)
            .setShowTitle(true)
            .build()
            .launchUrl(context, Uri.parse(url))
    }
}

/**
 * Resultado que llega por el deep link zaror://pago?estado=ok|error.
 * MainActivity lo publica aquí; el Scaffold del vecino lo muestra en un Snackbar.
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
