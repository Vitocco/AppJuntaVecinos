package com.example.appjuntavecinos.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appjuntavecinos.ui.components.Pestana
import com.example.appjuntavecinos.ui.screens.comun.FinanzasScreen
import com.example.appjuntavecinos.ui.screens.comun.InformeDetalleScreen
import com.example.appjuntavecinos.ui.screens.comun.PerfilScreen
import com.example.appjuntavecinos.ui.screens.vecino.MisReservasScreen
import com.example.appjuntavecinos.ui.screens.vecino.PagoWebpayScreen
import com.example.appjuntavecinos.ui.screens.vecino.SolicitarArriendoScreen
import com.example.appjuntavecinos.ui.screens.vecino.VecinoInicioScreen
import com.example.appjuntavecinos.util.EventosPago
import com.example.appjuntavecinos.util.programarAviso
import kotlinx.coroutines.launch

private val pestanasVecino = listOf(
    Pestana(RutasVecino.INICIO, "Inicio", Icons.Outlined.Home),
    Pestana(RutasVecino.RESERVAS, "Reservas", Icons.Outlined.CalendarMonth),
    Pestana(RutasVecino.FINANZAS, "Finanzas", Icons.Outlined.BarChart),
    Pestana(RutasVecino.PERFIL, "Perfil", Icons.Outlined.Person)
)

/** Grafo y Scaffold del VECINO. */
@Composable
fun VecinoNavegacion() {
    val nav = rememberNavController()
    val mensajes = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()    // vive mientras el vecino esté logueado
    val context = LocalContext.current

    // Vuelta desde Webpay (deep link zaror://pago?estado=...): avisar el resultado.
    // Las pantallas recargan solas al volver a verse (AlVolverAPantalla).
    val resultadoPago by EventosPago.resultado.collectAsState()
    LaunchedEffect(resultadoPago) {
        resultadoPago?.let { aprobado ->
            EventosPago.consumir()   // reinicia este efecto: el Snackbar va en "alcance" para no cancelarse
            alcance.launch {
                mensajes.showSnackbar(if (aprobado) "¡Pago aprobado! Tu reserva quedó pagada." else "El pago no se completó.")
            }
        }
    }

    ScaffoldRol(nav, pestanasVecino, mensajes) { modifier ->
        NavHost(navController = nav, startDestination = RutasVecino.INICIO, modifier = modifier) {
            composable(RutasVecino.INICIO) {
                VecinoInicioScreen(
                    onSolicitarArriendo = { nav.navigate(RutasVecino.SOLICITAR) },
                    onVerReservas = { nav.irAPestana(RutasVecino.RESERVAS) },
                    onPagarWebpay = { nav.navigate(RutasVecino.webpay(it)) }
                )
            }
            composable(RutasVecino.RESERVAS) {
                MisReservasScreen(onPagarWebpay = { nav.navigate(RutasVecino.webpay(it)) })
            }
            composable(RutasVecino.FINANZAS) {
                FinanzasScreen(onInforme = { nav.navigate(RutasVecino.informe(it)) })
            }
            composable(RutasVecino.PERFIL) { PerfilScreen() }

            composable(RutasVecino.SOLICITAR) {
                SolicitarArriendoScreen(
                    onVolver = { nav.popBackStack() },
                    onEnviada = {
                        // Notificación local (WorkManager) + aviso en pantalla
                        programarAviso(context, "Solicitud recibida", "La directiva revisará tu reserva")
                        alcance.launch { mensajes.showSnackbar("Solicitud enviada. La directiva la revisará.") }
                        nav.popBackStack()
                    }
                )
            }
            composable(
                RutasVecino.INFORME,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { InformeDetalleScreen(onVolver = { nav.popBackStack() }) }
            composable(
                RutasVecino.WEBPAY,
                arguments = listOf(navArgument("url") { type = NavType.StringType })
            ) { entrada ->
                PagoWebpayScreen(
                    url = entrada.arguments?.getString("url").orEmpty(),
                    // solo vuelve si seguimos en el WebView (evita sacar también la pestaña de abajo)
                    onTerminado = {
                        if (nav.currentDestination?.route == RutasVecino.WEBPAY) nav.popBackStack()
                    }
                )
            }
        }
    }
}
