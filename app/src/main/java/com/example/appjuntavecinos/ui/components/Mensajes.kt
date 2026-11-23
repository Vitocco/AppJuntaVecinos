package com.example.appjuntavecinos.ui.components

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.appjuntavecinos.ui.theme.CarbonIcono
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.TerracotaSuave

/**
 * Cada rol tiene UN Scaffold con su SnackbarHost. Las pantallas lo obtienen con
 * LocalMensajes.current y muestran avisos cortos ("Reserva cancelada").
 */
val LocalMensajes = compositionLocalOf<SnackbarHostState> { error("Falta el SnackbarHostState del rol") }

/** Snackbar con los colores de la app. */
@Composable
fun SnackbarZaror(estado: SnackbarHostState, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    SnackbarHost(estado, modifier) { datos ->
        Snackbar(
            snackbarData = datos,
            containerColor = CarbonIcono,
            contentColor = Crema,
            actionColor = TerracotaSuave,
            shape = androidx.compose.material3.MaterialTheme.shapes.small
        )
    }
}

/**
 * Muestra en el Snackbar el aviso que dejó el ViewModel (ej: "Perfil actualizado")
 * y le avisa que ya se mostró para que no se repita al girar la pantalla.
 */
@Composable
fun MostrarMensaje(mensaje: String?, onMostrado: () -> Unit) {
    val snackbar = LocalMensajes.current
    val alcance = rememberCoroutineScope()
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            onMostrado()   // esto cambia "mensaje" a null y reinicia este efecto...
            // ...por eso el Snackbar se lanza en OTRO alcance: si no, se cancelaría al instante
            alcance.launch { snackbar.showSnackbar(mensaje) }
        }
    }
}

/**
 * Ejecuta [accion] cada vez que la pantalla vuelve a estar visible (ON_RESUME):
 * al entrar, al volver de otra pantalla o al regresar a la app.
 * Así las listas se refrescan con lo último del backend.
 */
@Composable
fun AlVolverAPantalla(accion: () -> Unit) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { accion() }
}
