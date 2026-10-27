package com.example.appjuntavecinos.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appjuntavecinos.ui.components.BottomBar
import com.example.appjuntavecinos.ui.screens.HistorialFinancieroScreen
import com.example.appjuntavecinos.ui.screens.InicioScreen
import com.example.appjuntavecinos.ui.screens.LoginScreen
import com.example.appjuntavecinos.ui.screens.MisPagosScreen
import com.example.appjuntavecinos.ui.screens.RegistroScreen
import com.example.appjuntavecinos.ui.screens.RegistrarMovimientoScreen
import com.example.appjuntavecinos.ui.screens.SocioHomeScreen
import com.example.appjuntavecinos.ui.screens.SolicitarReservaScreen
import com.example.appjuntavecinos.util.programarAviso

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
                onLoginClick = { rol ->
                    val destino = if (rol == "SOCIO") Rutas.SOCIO_INICIO else Rutas.INICIO
                    navController.navigate(destino) { popUpTo(Rutas.LOGIN) { inclusive = true } }
                },
                onRegisterClick = { navController.navigate(Rutas.REGISTRO) }
            )
        }

        composable(Rutas.SOCIO_INICIO) {
            SocioHomeScreen(
                onSeleccionar = { if (it == 2) navController.navigate(Rutas.MOVIMIENTO) },
                onRegistrarMovimiento = { navController.navigate(Rutas.MOVIMIENTO) }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                onVolver = { navController.popBackStack() },
                onRegistroExitoso = { navController.popBackStack() }
            )
        }

        composable(Rutas.INICIO) {
            InicioScreen(
                onSeleccionar = { navController.navigate(pestanas[it]) },
                onSolicitarArriendo = { navController.navigate(Rutas.RESERVA) }
            )
        }

        composable(Rutas.RESERVA) {
            val context = LocalContext.current
            SolicitarReservaScreen(
                onVolver = { navController.popBackStack() },
                onSolicitudEnviada = {
                    // Notificación a los 10 s, aunque la app esté cerrada
                    programarAviso(context, "Solicitud recibida", "La directiva revisará tu reserva")
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.MIS_PAGOS) {
            MisPagosScreen(onSeleccionar = { navController.navigate(pestanas[it]) })
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