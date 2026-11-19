package com.example.appjuntavecinos.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appjuntavecinos.ui.components.Pestana
import com.example.appjuntavecinos.ui.screens.comun.InformeDetalleScreen
import com.example.appjuntavecinos.ui.screens.comun.PerfilScreen
import com.example.appjuntavecinos.ui.screens.tesorero.InformesScreen
import com.example.appjuntavecinos.ui.screens.tesorero.MovimientosScreen
import com.example.appjuntavecinos.ui.screens.tesorero.NuevoInformeScreen
import com.example.appjuntavecinos.ui.screens.tesorero.PagosScreen
import com.example.appjuntavecinos.ui.screens.tesorero.RegistrarMovimientoScreen
import com.example.appjuntavecinos.ui.screens.tesorero.RegistrarPagoScreen
import com.example.appjuntavecinos.ui.screens.tesorero.TesoreroInicioScreen
import kotlinx.coroutines.launch

private val pestanasTesorero = listOf(
    Pestana(RutasTesorero.INICIO, "Inicio", Icons.Outlined.Home),
    Pestana(RutasTesorero.MOVIMIENTOS, "Movimientos", Icons.AutoMirrored.Outlined.List),
    Pestana(RutasTesorero.PAGOS, "Pagos", Icons.Outlined.CreditCard),
    Pestana(RutasTesorero.INFORMES, "Informes", Icons.Outlined.Description),
    Pestana(RutasTesorero.PERFIL, "Perfil", Icons.Outlined.Person)
)

/** Grafo y Scaffold del TESORERO. */
@Composable
fun TesoreroNavegacion() {
    val nav = rememberNavController()
    val mensajes = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()

    // Vuelve atrás y muestra el aviso (el Snackbar vive en el Scaffold del rol)
    fun volverConAviso(texto: String) {
        alcance.launch { mensajes.showSnackbar(texto) }
        nav.popBackStack()
    }

    ScaffoldRol(nav, pestanasTesorero, mensajes) { modifier ->
        NavHost(navController = nav, startDestination = RutasTesorero.INICIO, modifier = modifier) {
            composable(RutasTesorero.INICIO) {
                TesoreroInicioScreen(
                    onIngreso = { nav.navigate(RutasTesorero.nuevoMovimiento("INGRESO")) },
                    onGasto = { nav.navigate(RutasTesorero.nuevoMovimiento("GASTO")) },
                    onBoleta = { nav.navigate(RutasTesorero.nuevoMovimiento("GASTO", camara = true)) },
                    onVerPagos = { nav.irAPestana(RutasTesorero.PAGOS) },
                    onVerMovimientos = { nav.irAPestana(RutasTesorero.MOVIMIENTOS) },
                    onRegistrarPago = { nav.navigate(RutasTesorero.pago(it)) }
                )
            }
            composable(RutasTesorero.MOVIMIENTOS) {
                MovimientosScreen(
                    onNuevo = { nav.navigate(RutasTesorero.nuevoMovimiento("INGRESO")) },
                    onEditar = { nav.navigate(RutasTesorero.editarMovimiento(it.id, it.fechaMovimiento)) }
                )
            }
            composable(RutasTesorero.PAGOS) {
                PagosScreen(onRegistrarPago = { nav.navigate(RutasTesorero.pago(it)) })
            }
            composable(RutasTesorero.INFORMES) {
                InformesScreen(
                    onInforme = { nav.navigate(RutasTesorero.informe(it)) },
                    onNuevo = { nav.navigate(RutasTesorero.NUEVO_INFORME) }
                )
            }
            composable(RutasTesorero.PERFIL) { PerfilScreen() }

            // ---- Pantallas sin barra inferior ----
            composable(
                RutasTesorero.MOVIMIENTO,
                arguments = listOf(
                    navArgument("tipo") { type = NavType.StringType; defaultValue = "INGRESO" },
                    navArgument("id") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("fecha") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("camara") { type = NavType.BoolType; defaultValue = false }
                )
            ) {
                RegistrarMovimientoScreen(
                    onVolver = { nav.popBackStack() },
                    onGuardado = { volverConAviso("Movimiento guardado") }
                )
            }
            composable(
                RutasTesorero.PAGO,
                arguments = listOf(navArgument("reservaId") { type = NavType.LongType })
            ) {
                RegistrarPagoScreen(
                    onVolver = { nav.popBackStack() },
                    onGuardado = { volverConAviso("Pago registrado: la reserva quedó pagada") }
                )
            }
            composable(
                RutasTesorero.INFORME,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { InformeDetalleScreen(onVolver = { nav.popBackStack() }) }
            composable(RutasTesorero.NUEVO_INFORME) {
                NuevoInformeScreen(
                    onVolver = { nav.popBackStack() },
                    onGuardado = { volverConAviso("Informe creado") }
                )
            }
        }
    }
}
