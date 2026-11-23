package com.example.appjuntavecinos.ui.screens.vecino

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.data.remote.dto.Feriado
import com.example.appjuntavecinos.ui.components.AlRecibirUrlWebpay
import com.example.appjuntavecinos.ui.components.AlVolverAPantalla
import com.example.appjuntavecinos.ui.components.BotonAccion
import com.example.appjuntavecinos.ui.components.BotonPrincipal
import com.example.appjuntavecinos.ui.components.EncabezadoHome
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.HojaPago
import com.example.appjuntavecinos.ui.components.FilaReserva
import com.example.appjuntavecinos.ui.components.MostrarMensaje
import com.example.appjuntavecinos.ui.components.PantallaLista
import com.example.appjuntavecinos.ui.components.PedirPermisoNotificaciones
import com.example.appjuntavecinos.ui.components.Tarjeta
import com.example.appjuntavecinos.ui.components.TarjetaSaldo
import com.example.appjuntavecinos.ui.components.TituloSeccion
import com.example.appjuntavecinos.ui.components.seccion
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.viewmodel.VecinoInicioViewModel
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.Moneda
import com.example.appjuntavecinos.util.esFutura
import com.example.appjuntavecinos.util.porPagar

/**
 * Inicio del Vecino (diseño 3): "Hola, <nombre>", saldo de la junta del mes,
 * botón para solicitar arriendo y sus 2 próximas reservas. Las APROBADAS sin pagar
 * muestran "Pagar $X", que abre la hoja de medios de pago.
 */
@Composable
fun VecinoInicioScreen(
    onSolicitarArriendo: () -> Unit,
    onVerReservas: () -> Unit,
    onPagarWebpay: (String) -> Unit,
    viewModel: VecinoInicioViewModel = viewModel()
) {
    val sesion by viewModel.sesion.collectAsState()
    val resumen by viewModel.resumen.collectAsState()
    val proximas by viewModel.proximas.collectAsState()
    val pagando by viewModel.pagando.collectAsState()
    val abrirUrl by viewModel.abrirUrl.collectAsState()
    val hojaPago by viewModel.hojaPago.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val feriados by viewModel.feriados.collectAsState()

    PedirPermisoNotificaciones()
    LaunchedEffect(Unit) { viewModel.cargarFeriados() }
    AlVolverAPantalla { viewModel.cargar() }
    MostrarMensaje(mensaje, viewModel::mensajeMostrado)
    AlRecibirUrlWebpay(abrirUrl, viewModel::urlAbierta, onPagarWebpay)

    hojaPago?.let { r ->
        HojaPago(
            reserva = r,
            iniciandoWebpay = pagando == r.id,
            // pagos/webpay/iniciar -> PagoWebpayScreen
            onIrAWebpay = viewModel::pagarConWebpay,
            onCerrar = viewModel::cerrarHojaPago
        )
    }

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
                            texto = "Pagar ${Moneda.pesos(reserva.precio)}",
                            onClick = { viewModel.abrirHojaPago(reserva) },
                            cargando = pagando == reserva.id
                        )
                    }) else null
                )
            }
        }

        // Sin feriados (ej: sin internet) no se muestra la tarjeta
        if (feriados.isNotEmpty()) item { TarjetaFeriados(feriados) }
    }
}

/** "Próximos feriados": una fila por feriado, "25 dic · Navidad". */
@Composable
private fun TarjetaFeriados(feriados: List<Feriado>) {
    Tarjeta {
        Text("Próximos feriados", style = MaterialTheme.typography.titleMedium, color = Crema)
        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            feriados.forEach { f ->
                val dia = runCatching { Fechas.diaMes(Fechas.leer(f.date)) }.getOrDefault(f.date)
                Text("$dia · ${f.localName}", style = MaterialTheme.typography.bodyLarge, color = GrisCalido)
            }
        }
    }
}
