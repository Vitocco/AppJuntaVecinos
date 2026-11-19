package com.example.appjuntavecinos.ui.screens.vecino

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.AbrirWebpay
import com.example.appjuntavecinos.ui.components.AlVolverAPantalla
import com.example.appjuntavecinos.ui.components.BotonAccion
import com.example.appjuntavecinos.ui.components.BotonPrincipal
import com.example.appjuntavecinos.ui.components.EncabezadoHome
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.FilaReserva
import com.example.appjuntavecinos.ui.components.MostrarMensaje
import com.example.appjuntavecinos.ui.components.PantallaLista
import com.example.appjuntavecinos.ui.components.PedirPermisoNotificaciones
import com.example.appjuntavecinos.ui.components.TarjetaSaldo
import com.example.appjuntavecinos.ui.components.TituloSeccion
import com.example.appjuntavecinos.ui.components.seccion
import com.example.appjuntavecinos.ui.viewmodel.VecinoInicioViewModel
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.Moneda
import com.example.appjuntavecinos.util.esFutura
import com.example.appjuntavecinos.util.porPagar

/**
 * Inicio del Vecino (diseño 3): "Hola, <nombre>", saldo de la junta del mes,
 * botón para solicitar arriendo y sus 2 próximas reservas. Las APROBADAS sin pagar
 * muestran "Pagar $X con Webpay".
 */
@Composable
fun VecinoInicioScreen(
    onSolicitarArriendo: () -> Unit,
    onVerReservas: () -> Unit,
    viewModel: VecinoInicioViewModel = viewModel()
) {
    val sesion by viewModel.sesion.collectAsState()
    val resumen by viewModel.resumen.collectAsState()
    val proximas by viewModel.proximas.collectAsState()
    val pagando by viewModel.pagando.collectAsState()
    val abrirUrl by viewModel.abrirUrl.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    PedirPermisoNotificaciones()
    AlVolverAPantalla { viewModel.cargar() }
    MostrarMensaje(mensaje, viewModel::mensajeMostrado)
    AbrirWebpay(abrirUrl, viewModel::urlAbierta)

    PantallaLista(onRefrescar = viewModel::cargar) {
        item { EncabezadoHome(saludo = "Hola,", titulo = sesion?.nombre.orEmpty()) }

        seccion(resumen, viewModel::cargar) { r ->
            item {
                TarjetaSaldo(
                    etiqueta = "Saldo de la junta · ${Fechas.nombreMes(viewModel.mes)}",
                    saldo = r.saldo,
                    ingresos = r.ingresosMes,
                    gastos = r.gastosMes
                )
            }
        }

        item { BotonPrincipal("Solicitar arriendo de la sede", onClick = onSolicitarArriendo, icono = Icons.Outlined.Add) }

        item { TituloSeccion("Mis reservas", accion = "Ver todas", onAccion = onVerReservas) }
        seccion(proximas, viewModel::cargar) { lista ->
            if (lista.isEmpty()) item {
                EstadoVacio("No tienes reservas próximas", mensaje = "Solicita el arriendo de una sede y aparecerá aquí.")
            }
            items(lista, key = { it.id }) { reserva ->
                FilaReserva(
                    reserva = reserva,
                    contenidoExtra = if (reserva.porPagar && reserva.esFutura) ({
                        BotonAccion(
                            texto = "Pagar ${Moneda.pesos(reserva.precio)} con Webpay",
                            onClick = { viewModel.pagar(reserva) },
                            cargando = pagando == reserva.id
                        )
                    }) else null
                )
            }
        }
    }
}
