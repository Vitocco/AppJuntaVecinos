package com.example.appjuntavecinos.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appjuntavecinos.ui.components.Pestana
import com.example.appjuntavecinos.ui.screens.comun.FinanzasScreen
import com.example.appjuntavecinos.ui.screens.comun.InformeDetalleScreen
import com.example.appjuntavecinos.ui.screens.comun.PerfilScreen
import com.example.appjuntavecinos.ui.screens.directiva.CalendarioScreen
import com.example.appjuntavecinos.ui.screens.directiva.DirectivaInicioScreen
import com.example.appjuntavecinos.ui.screens.directiva.SedeDetalleScreen
import com.example.appjuntavecinos.ui.screens.directiva.SolicitudesScreen

private val pestanasDirectiva = listOf(
    Pestana(RutasDirectiva.INICIO, "Inicio", Icons.Outlined.Home),
    Pestana(RutasDirectiva.SOLICITUDES, "Solicitudes", Icons.Outlined.Inbox),
    Pestana(RutasDirectiva.CALENDARIO, "Calendario", Icons.Outlined.CalendarMonth),
    Pestana(RutasDirectiva.FINANZAS, "Finanzas", Icons.Outlined.BarChart),
    Pestana(RutasDirectiva.PERFIL, "Perfil", Icons.Outlined.Person)
)

/** Grafo y Scaffold de la DIRECTIVA. */
@Composable
fun DirectivaNavegacion() {
    val nav = rememberNavController()
    val mensajes = remember { SnackbarHostState() }

    ScaffoldRol(nav, pestanasDirectiva, mensajes) { modifier ->
        NavHost(navController = nav, startDestination = RutasDirectiva.INICIO, modifier = modifier) {
            composable(RutasDirectiva.INICIO) {
                DirectivaInicioScreen(onVerSolicitudes = { nav.irAPestana(RutasDirectiva.SOLICITUDES) })
            }
            composable(RutasDirectiva.SOLICITUDES) { SolicitudesScreen() }
            composable(RutasDirectiva.CALENDARIO) {
                CalendarioScreen(onSede = { nav.navigate(RutasDirectiva.sede(it)) })
            }
            composable(RutasDirectiva.FINANZAS) {
                FinanzasScreen(onInforme = { nav.navigate(RutasDirectiva.informe(it)) })
            }
            composable(RutasDirectiva.PERFIL) { PerfilScreen() }

            composable(
                RutasDirectiva.SEDE,
                arguments = listOf(navArgument("sedeId") { type = NavType.LongType })
            ) { SedeDetalleScreen(onVolver = { nav.popBackStack() }) }
            composable(
                RutasDirectiva.INFORME,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { InformeDetalleScreen(onVolver = { nav.popBackStack() }) }
        }
    }
}
