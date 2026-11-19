package com.example.appjuntavecinos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appjuntavecinos.data.session.SesionManager
import com.example.appjuntavecinos.ui.components.EstadoCarga
import com.example.appjuntavecinos.ui.components.FondoZaror
import com.example.appjuntavecinos.ui.components.PedirPermisoRedLocal
import com.example.appjuntavecinos.ui.screens.LoginScreen
import com.example.appjuntavecinos.ui.screens.RegistroScreen

/**
 * Navegación principal: Login, Registro y un destino por rol.
 * Cada rol tiene su propio Scaffold + grafo (VecinoNavegacion, DirectivaNavegacion, TesoreroNavegacion).
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val cargada by SesionManager.cargada.collectAsState()
    val sesion by SesionManager.sesion.collectAsState()

    // Android 17: pide el permiso de red local para poder llegar al backend
    PedirPermisoRedLocal()

    // 1° Mientras se lee la sesión guardada en DataStore (una fracción de segundo)
    if (!cargada) {
        FondoZaror { EstadoCarga() }
        return
    }

    // 2° Pantalla inicial: si ya había sesión, directo al grafo de su rol
    val inicio = remember { sesion?.let { Rutas.inicioDe(it.rol) } ?: Rutas.LOGIN }

    // 3° Si la sesión desaparece (Cerrar sesión o 401 = token vencido) se vuelve al login
    //    borrando todo el historial, para que "atrás" no regrese a pantallas privadas.
    LaunchedEffect(sesion) {
        val rutaActual = navController.currentDestination?.route
        if (sesion == null && rutaActual != null && rutaActual !in Rutas.publicas) {
            navController.irLimpiando(Rutas.LOGIN)
        }
    }

    NavHost(navController = navController, startDestination = inicio) {

        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginClick = { rol -> navController.irLimpiando(Rutas.inicioDe(rol)) },
                onRegisterClick = { navController.navigate(Rutas.REGISTRO) }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                onVolver = { navController.popBackStack() },
                // el registro deja la sesión iniciada como VECINO
                onRegistroExitoso = { navController.irLimpiando(Rutas.VECINO) }
            )
        }

        // Un destino por rol. SoloParaRol impide entrar con otro rol.
        composable(Rutas.VECINO) { SoloParaRol(Rutas.VECINO, sesion?.rol, navController) { VecinoNavegacion() } }
        composable(Rutas.DIRECTIVA) { SoloParaRol(Rutas.DIRECTIVA, sesion?.rol, navController) { DirectivaNavegacion() } }
        composable(Rutas.TESORERO) { SoloParaRol(Rutas.TESORERO, sesion?.rol, navController) { TesoreroNavegacion() } }
    }
}

/**
 * Guardia: muestra [contenido] solo si el rol de la sesión corresponde a [ruta].
 * Si no (o no hay sesión), redirige al grafo correcto o al login.
 */
@Composable
private fun SoloParaRol(
    ruta: String,
    rol: String?,
    navController: NavHostController,
    contenido: @Composable () -> Unit
) {
    if (rol != null && Rutas.inicioDe(rol) == ruta) {
        contenido()
    } else {
        LaunchedEffect(rol) {
            navController.irLimpiando(if (rol == null) Rutas.LOGIN else Rutas.inicioDe(rol))
        }
    }
}

/** Navega a [ruta] borrando TODO el historial (al iniciar o cerrar sesión). */
fun NavHostController.irLimpiando(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
