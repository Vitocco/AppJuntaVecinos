package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.data.remote.dto.ReservaDto
import com.example.appjuntavecinos.ui.theme.Carbon
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.util.cuando

/** Ícono y color según el estado: ✓ aprobada, reloj pendiente, X rechazada, ⊘ cancelada. */
fun iconoDeEstado(estado: String): Pair<ImageVector, Color> = when (estado) {
    "APROBADA", "PAGADO" -> Icons.Outlined.Check to Tono.OK.texto
    "PENDIENTE" -> Icons.Outlined.Schedule to Tono.PENDIENTE.texto
    "RECHAZADA", "RECHAZADO" -> Icons.Outlined.Close to Tono.ERROR.texto
    else -> Icons.Outlined.Block to GrisCalido
}

/**
 * Fila compacta de una reserva (Inicio de cada rol):
 *   [✓]  Cumpleaños familiar                [Aprobada]
 *        Sáb 22 nov · 18:00 a 23:00
 */
@Composable
fun FilaReserva(
    reserva: ReservaDto,
    modifier: Modifier = Modifier,
    titulo: String = reserva.motivo ?: reserva.sedeNombre.orEmpty(),
    subtitulo: String = reserva.cuando,
    mostrarPill: Boolean = true,
    onClick: (() -> Unit)? = null,
    contenidoExtra: (@Composable ColumnScope.() -> Unit)? = null
) {
    val (icono, color) = iconoDeEstado(reserva.estado)
    FilaItem(
        titulo = titulo,
        subtitulo = subtitulo,
        icono = icono,
        colorIcono = color,
        modifier = modifier,
        onClick = onClick,
        derecha = if (mostrarPill) ({ PillEstado(reserva.estado) }) else null,
        contenidoExtra = contenidoExtra
    )
}

/**
 * Tarjeta con la foto de la sede (diseño 6, "Mis reservas"):
 * foto arriba con el nombre de la sede y el estado encima; abajo el motivo,
 * "fecha · horario · personas" y los botones que correspondan.
 */
@Composable
fun TarjetaReservaFoto(
    reserva: ReservaDto,
    modifier: Modifier = Modifier,
    acciones: (@Composable ColumnScope.() -> Unit)? = null
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(CarbonCampo)
    ) {
        Box(Modifier.fillMaxWidth().height(150.dp)) {
            FotoSede(ruta = reserva.sedeFoto, descripcion = reserva.sedeNombre, modifier = Modifier.fillMaxWidth().height(150.dp))
            PillEstado(reserva.estado, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
            Text(
                reserva.sedeNombre.orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = Crema,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Carbon.copy(alpha = 0.78f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(reserva.motivo ?: reserva.sedeNombre.orEmpty(), style = MaterialTheme.typography.titleLarge, color = Crema)
            Text(
                "${reserva.cuando} · ${reserva.asistentes} personas",
                style = MaterialTheme.typography.bodyMedium,
                color = GrisCalido
            )
            if (!reserva.observacion.isNullOrBlank()) {
                Text("“${reserva.observacion}”", style = MaterialTheme.typography.bodySmall, color = GrisCalido,
                    modifier = Modifier.padding(top = 4.dp))
            }
            if (acciones != null) {
                Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { acciones() }
            }
        }
    }
}

/**
 * Solicitud para la Directiva (diseño 3):
 *   [reloj]  Camila Rojas · Reunión                   [Pendiente]
 *            Mié 26 nov · 19:00 a 21:00 · 25 pers. · Salón principal
 *   [ Aprobar ]  [ Rechazar ]      <- solo si está PENDIENTE
 */
@Composable
fun TarjetaSolicitud(
    reserva: ReservaDto,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendiente = reserva.estado == "PENDIENTE"
    FilaReserva(
        reserva = reserva,
        modifier = modifier,
        titulo = "${reserva.usuarioNombre} · ${reserva.motivo ?: ""}",
        subtitulo = "${reserva.cuando} · ${reserva.asistentes} pers. · ${reserva.sedeNombre}",
        mostrarPill = !pendiente,
        contenidoExtra = if (pendiente) ({
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonTonal("Aprobar", onAprobar, Tono.OK.texto, Tono.OK.fondo, Modifier.weight(1f))
                BotonTonal("Rechazar", onRechazar, Tono.ERROR.texto, Tono.ERROR.fondo, Modifier.weight(1f))
            }
        }) else null
    )
}

/** Diálogo de Aprobar/Rechazar con observación opcional, para una [Decision]. */
@Composable
fun DialogoDecision(
    decision: com.example.appjuntavecinos.ui.viewmodel.Decision,
    cargando: Boolean,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    val r = decision.reserva
    DialogoObservacion(
        titulo = if (decision.aprobar) "Aprobar solicitud" else "Rechazar solicitud",
        detalle = "${r.usuarioNombre} · ${r.motivo}\n${r.sedeNombre} · ${r.cuando}",
        textoConfirmar = if (decision.aprobar) "Aprobar" else "Rechazar",
        aprobar = decision.aprobar,
        cargando = cargando,
        onConfirmar = onConfirmar,
        onCancelar = onCancelar
    )
}
