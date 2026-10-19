package com.example.appjuntavecinos.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appjuntavecinos.ui.screens.HistorialFinancieroScreen
import com.example.appjuntavecinos.ui.screens.InicioScreen
import com.example.appjuntavecinos.ui.screens.LoginScreen
import com.example.appjuntavecinos.ui.screens.RegisterScreen
import com.example.appjuntavecinos.ui.screens.RegistrarMovimientoScreen
import com.example.appjuntavecinos.ui.screens.SolicitarReservaScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val pestanas = listOf(Rutas.INICIO, Rutas.RESERVA, Rutas.MIS_PAGOS, Rutas.HISTORIAL)

    // El "escenario": muestra la pantalla de la ruta actual.
    // startDestination = la primera pantalla al abrir la app.
    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN

    ) {
        // Cada composable(ruta) { ... } = "cuando la ruta sea X, muestra esta pantalla".

        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginClick = { navController.navigate(Rutas.INICIO) },
                onRegisterClick = { navController.navigate(Rutas.REGISTER) }
            )
        }

        composable(Rutas.REGISTER) {
            RegisterScreen(
                onVolver = { navController.popBackStack() },
                onRegistroExitoso = { navController.popBackStack() }
            )
        }

        composable(Rutas.INICIO) {
            InicioScreen(onSeleccionar = { navController.navigate(pestanas[it]) })
        }

        composable(Rutas.RESERVA) {
            SolicitarReservaScreen(
                onVolver = { navController.popBackStack() },
                onSolicitudEnviada = { navController.popBackStack() }
            )
        }

        composable(Rutas.MIS_PAGOS) { Text("Mis pagos: próximamente") }

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