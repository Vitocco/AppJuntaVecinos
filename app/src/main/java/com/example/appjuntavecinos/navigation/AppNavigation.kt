package com.example.appjuntavecinos.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appjuntavecinos.ui.screens.InicioScreen
import com.example.appjuntavecinos.ui.screens.LoginScreen
import com.example.appjuntavecinos.ui.screens.HistorialFinancieroScreen
import com.example.appjuntavecinos.ui.screens.RegistrarMovimientoScreen
import com.example.appjuntavecinos.ui.screens.SolicitarReservaScreen

@Composable
fun AppNavigation() {
    // El "conductor": recuerda en qué pantalla estás y el historial (para el botón atrás).
    // "remember" → se crea UNA vez y no se pierde al redibujar.
    val navController = rememberNavController()

    // El "escenario": muestra la pantalla de la ruta actual.
    // startDestination = la primera pantalla al abrir la app.
    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN

    ) {
        // Cada composable(ruta) { ... } = "cuando la ruta sea X, muestra esta pantalla".

        composable(Rutas.LOGIN) {
            LoginScreen(
                // Aquí se CONECTA el aviso del login con el viaje.
                // El LoginScreen no sabe adónde va; el mapa sí.
                onLoginClick = {
                    navController.navigate(Rutas.INICIO) {
                        // Saca el Login del historial: al apretar "atrás" en Inicio
                        // la app se cierra en vez de volver al login (ya iniciaste sesión).
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.INICIO) {
            InicioScreen()
        }

        composable(Rutas.RESERVA) {
            SolicitarReservaScreen(
                onVolver = { navController.popBackStack() },
                onSolicitudEnviada = { navController.popBackStack() }
            )
        }

        composable(Rutas.HISTORIAL) {
            HistorialFinancieroScreen()
        }

        composable(Rutas.MOVIMIENTO) {
            RegistrarMovimientoScreen(
                onVolver = { navController.popBackStack() },
                onMovimientoGuardado = { navController.popBackStack() }
            )
        }
    }
}