package com.example.appjuntavecinos.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appjuntavecinos.ui.components.BarraInferior
import com.example.appjuntavecinos.ui.components.LocalMensajes
import com.example.appjuntavecinos.ui.components.Pestana
import com.example.appjuntavecinos.ui.components.SnackbarZaror
import com.example.appjuntavecinos.ui.theme.Carbon

/**
 * Scaffold de un rol: BarraInferior con SUS pestañas + Snackbar.
 * La barra solo se ve en las pestañas (no en "Solicitar arriendo", "Registrar pago", etc.).
 * [contenido] recibe el Modifier con el espacio que ocupa la barra.
 */
@Composable
fun ScaffoldRol(
    navController: NavHostController,
    pestanas: List<Pestana>,
    mensajes: SnackbarHostState,
    contenido: @Composable (Modifier) -> Unit
) {
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    val enPestana = pestanas.any { it.ruta == rutaActual }

    CompositionLocalProvider(LocalMensajes provides mensajes) {
        Scaffold(
            containerColor = Carbon,
            contentWindowInsets = WindowInsets(0),     // cada pantalla maneja la barra de estado
            snackbarHost = { SnackbarZaror(mensajes) },
            bottomBar = {
                if (enPestana) {
                    BarraInferior(pestanas, rutaActual) { navController.irAPestana(it.ruta) }
                }
            }
        ) { relleno: PaddingValues ->
            // consumeWindowInsets: si la barra ya cubre la zona de navegación del sistema,
            // las pantallas no vuelven a sumar ese espacio.
            contenido(Modifier.padding(relleno).consumeWindowInsets(relleno))
        }
    }
}

/**
 * Cambia de pestaña sin apilar pantallas: guarda el estado de la pestaña que se deja
 * y restaura el de la pestaña a la que se vuelve (scroll, filtros...).
 */
fun NavHostController.irAPestana(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
