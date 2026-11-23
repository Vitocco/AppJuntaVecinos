package com.example.appjuntavecinos.ui.screens.vecino

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.appjuntavecinos.BuildConfig
import com.example.appjuntavecinos.ui.theme.Carbon
import com.example.appjuntavecinos.ui.theme.CarbonIcono
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.Terracota
import com.example.appjuntavecinos.util.EventosPago

/**
 * Formulario de pago de Webpay (Transbank, ambiente de integración) dentro de un WebView,
 * sin Chrome. [url] ya trae  ?token_ws=<token>  (Webpay.urlFormulario).
 *
 *  - Cuando el backend termina redirige a zaror://pago?estado=ok|error: el WebView NO carga
 *    esa URL, publica el resultado en [EventosPago] (el Scaffold del vecino muestra el Snackbar)
 *    y llama a [onTerminado].
 *  - Cerrar (X o Atrás) sin terminar también llama a [onTerminado]; la reserva sigue "Por pagar".
 *  - Al volver, Inicio / Mis reservas recargan reservas/mias solas (AlVolverAPantalla).
 *  - FLAG_SECURE (solo en release) mientras esta pantalla está abierta: sin capturas ni grabación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")   // el formulario de Transbank necesita JavaScript
@Composable
fun PagoWebpayScreen(url: String, onTerminado: () -> Unit) {
    var cargando by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Protección de pantalla (FLAG_SECURE): en producción (release) la pantalla de pago queda
    // protegida contra capturas y grabaciones mientras está abierta. En debug NO se activa,
    // para poder grabar el video de la app.
    DisposableEffect(Unit) {
        val ventana = if (!BuildConfig.DEBUG) context.buscarActivity()?.window else null
        ventana?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { ventana?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }

    BackHandler(onBack = onTerminado)

    Column(Modifier.fillMaxSize().background(Carbon)) {
        TopAppBar(
            title = { Text("Pago con Webpay", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onTerminado) {
                    Icon(Icons.Outlined.Close, contentDescription = "Cerrar")
                }
            },
            windowInsets = WindowInsets(0),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Carbon,
                titleContentColor = Crema,
                navigationIconContentColor = Crema
            ),
            modifier = Modifier.statusBarsPadding()
        )
        // Indicador de carga (deja el espacio fijo para que la página no salte)
        if (cargando) {
            LinearProgressIndicator(color = Terracota, trackColor = CarbonIcono, modifier = Modifier.fillMaxWidth())
        } else {
            Spacer(Modifier.height(4.dp))
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        private var terminado = false   // evita volver dos veces

                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                            interceptarVuelta(request.url)

                        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                            // Respaldo por si la redirección no pasa por shouldOverrideUrlLoading
                            if (url != null && interceptarVuelta(Uri.parse(url))) view.stopLoading()
                            else cargando = true
                        }

                        override fun onPageFinished(view: WebView, url: String?) { cargando = false }

                        /** zaror://pago?estado=ok|error -> no se carga: se avisa y se cierra. */
                        private fun interceptarVuelta(uri: Uri): Boolean {
                            if (!uri.toString().startsWith("zaror://pago")) return false
                            if (!terminado) {
                                terminado = true
                                EventosPago.recibir(uri)
                                onTerminado()
                            }
                            return true
                        }
                    }
                    loadUrl(url)
                }
            },
            onRelease = { it.destroy() }
        )
    }
}

private tailrec fun Context.buscarActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.buscarActivity()
    else -> null
}
