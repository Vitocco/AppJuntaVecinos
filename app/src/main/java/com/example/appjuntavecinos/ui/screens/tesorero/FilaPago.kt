package com.example.appjuntavecinos.ui.screens.tesorero

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.appjuntavecinos.ui.components.BotonPrincipal
import com.example.appjuntavecinos.ui.components.FilaItem
import com.example.appjuntavecinos.ui.components.PillEstado
import com.example.appjuntavecinos.ui.components.Tono
import com.example.appjuntavecinos.ui.viewmodel.ItemPago
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.Moneda

/** "TRANSFERENCIA" -> "Transferencia" */
private fun textoMedio(medio: String) = medio.lowercase().replaceFirstChar { it.uppercase() }

/**
 * Fila de "Pagos de reservas" (diseño 4):
 *  - Por pagar: reloj ámbar, "Camila Rojas · $35.000", "Sáb 22 nov · Cumpleaños" y botón "Registrar pago".
 *  - Pagado:    check verde, "Ana Soto · $35.000", "Transferencia · TRF-998877".
 */
@Composable
fun FilaPago(item: ItemPago, onRegistrarPago: (Long) -> Unit, modifier: Modifier = Modifier) {
    when (item) {
        is ItemPago.PorPagar -> {
            val r = item.reserva
            FilaItem(
                titulo = "${r.usuarioNombre} · ${Moneda.pesos(r.precio)}",
                subtitulo = "${Fechas.diaCompleto(Fechas.leer(r.fecha))} · ${r.motivo.orEmpty()} · ${r.sedeNombre}",
                icono = Icons.Outlined.Schedule,
                colorIcono = Tono.PENDIENTE.texto,
                modifier = modifier,
                derecha = { PillEstado("Por pagar", Tono.PENDIENTE) },
                contenidoExtra = { BotonPrincipal("Registrar pago", onClick = { onRegistrarPago(r.id) }) }
            )
        }
        is ItemPago.Pagado -> {
            val p = item.pago
            val detalle = listOfNotNull(
                textoMedio(p.medio),
                p.referencia?.ifBlank { null },
                p.fechaPago?.let { Fechas.diaMes(Fechas.leer(it)) }
            ).joinToString(" · ")
            FilaItem(
                titulo = "${p.usuarioNombre} · ${Moneda.pesos(p.monto)}",
                subtitulo = "${p.sedeNombre} · $detalle",
                icono = Icons.Outlined.Check,
                colorIcono = Tono.OK.texto,
                modifier = modifier,
                derecha = { PillEstado(p.estado) }
            )
        }
    }
}
