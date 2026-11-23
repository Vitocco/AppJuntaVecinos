package com.example.appjuntavecinos.ui.screens.vecino

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.AlRecibirUrlWebpay
import com.example.appjuntavecinos.ui.components.AlVolverAPantalla
import com.example.appjuntavecinos.ui.components.BotonAccion
import com.example.appjuntavecinos.ui.components.BotonTonal
import com.example.appjuntavecinos.ui.components.ChipFiltro
import com.example.appjuntavecinos.ui.components.DialogoConfirmacion
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.HojaPago
import com.example.appjuntavecinos.ui.components.MostrarMensaje
import com.example.appjuntavecinos.ui.components.PantallaLista
import com.example.appjuntavecinos.ui.components.TarjetaReservaFoto
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.components.Tono
import com.example.appjuntavecinos.ui.components.seccion
import com.example.appjuntavecinos.ui.viewmodel.FiltroReservas
import com.example.appjuntavecinos.ui.viewmodel.MisReservasViewModel
import com.example.appjuntavecinos.util.Moneda
import com.example.appjuntavecinos.util.cuando
import com.example.appjuntavecinos.util.esFutura
import com.example.appjuntavecinos.util.porPagar
import com.example.appjuntavecinos.util.sePuedeCancelar

/**
 * Mis reservas (diseño 6): chips Todas / Pendientes / Aprobadas / Pasadas y una tarjeta
 * con la foto de la sede por reserva. Se puede pagar (hoja de medios de pago) y cancelar con confirmación.
 */
@Composable
fun MisReservasScreen(
    onPagarWebpay: (String) -> Unit,
    viewModel: MisReservasViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val porCancelar by viewModel.porCancelar.collectAsState()
    val cancelando by viewModel.cancelando.collectAsState()
    val pagando by viewModel.pagando.collectAsState()
    val abrirUrl by viewModel.abrirUrl.collectAsState()
    val hojaPago by viewModel.hojaPago.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

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

    porCancelar?.let { r ->
        DialogoConfirmacion(
            titulo = "¿Cancelar reserva?",
            mensaje = "${r.motivo ?: r.sedeNombre} · ${r.cuando}. La sede quedará libre para otros vecinos.",
            textoConfirmar = "Sí, cancelar",
            peligroso = true,
            cargando = cancelando,
            onConfirmar = viewModel::confirmarCancelar,
            onCancelar = viewModel::descartarCancelar
        )
    }

    PantallaLista(onRefrescar = viewModel::cargar) {
        item { TextoConPunto("Mis reservas", estilo = MaterialTheme.typography.displaySmall) }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FiltroReservas.entries.forEach { f ->
                    ChipFiltro(f.texto, seleccionado = f == filtro, onClick = { viewModel.elegirFiltro(f) })
                }
            }
        }
        seccion(estado, viewModel::cargar) { todas ->
            val lista = todas.filter { filtro.aplicar(it) }
            if (lista.isEmpty()) item {
                EstadoVacio(
                    if (filtro == FiltroReservas.TODAS) "Aún no tienes reservas" else "No hay reservas ${filtro.texto.lowercase()}",
                    mensaje = if (filtro == FiltroReservas.TODAS) "Solicita el arriendo de una sede desde Inicio." else null
                )
            }
            items(lista, key = { it.id }) { reserva ->
                val puedePagar = reserva.porPagar && reserva.esFutura
                TarjetaReservaFoto(
                    reserva = reserva,
                    acciones = if (puedePagar || reserva.sePuedeCancelar) ({
                        if (puedePagar) {
                            BotonAccion(
                                texto = "Pagar ${Moneda.pesos(reserva.precio)}",
                                onClick = { viewModel.abrirHojaPago(reserva) },
                                cargando = pagando == reserva.id
                            )
                        }
                        if (reserva.sePuedeCancelar) {
                            BotonTonal(
                                texto = "Cancelar reserva",
                                onClick = { viewModel.pedirCancelar(reserva) },
                                colorTexto = Tono.ERROR.texto,
                                colorFondo = Tono.ERROR.fondo
                            )
                        }
                    }) else null
                )
            }
        }
    }
}
