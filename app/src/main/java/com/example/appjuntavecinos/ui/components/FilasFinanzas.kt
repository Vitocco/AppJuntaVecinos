package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.data.remote.dto.InformeDto
import com.example.appjuntavecinos.data.remote.dto.MovimientoDto
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.RojoError
import com.example.appjuntavecinos.ui.theme.VerdeOk
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.Moneda

/**
 * Un movimiento:  [↑ verde | ↓ rojo]  Descripción          + $ 35.000
 *                                      Categoría · 15 nov   (🧾 si tiene comprobante)
 * Los ANULADOS se ven en gris y tachados.
 */
@Composable
fun FilaMovimiento(
    movimiento: MovimientoDto,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val anulado = movimiento.estado == "ANULADO"
    val esIngreso = movimiento.tipo == "INGRESO"
    val color = when {
        anulado -> GrisCalido
        esIngreso -> VerdeOk
        else -> RojoError
    }
    val fecha = Fechas.diaMes(Fechas.leer(movimiento.fechaMovimiento))
    FilaItem(
        titulo = movimiento.descripcion?.ifBlank { null } ?: movimiento.categoriaNombre.orEmpty(),
        subtitulo = "${movimiento.categoriaNombre} · $fecha" + if (anulado) " · Anulado" else "",
        icono = if (esIngreso) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
        colorIcono = color,
        tachado = anulado,
        modifier = modifier,
        onClick = onClick,
        derecha = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (esIngreso) Moneda.ingreso(movimiento.monto) else Moneda.gasto(movimiento.monto),
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    textDecoration = if (anulado) TextDecoration.LineThrough else null
                )
                if (movimiento.tieneComprobante) {
                    Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = "Tiene comprobante",
                        tint = GrisCalido, modifier = Modifier.size(16.dp))
                }
            }
        }
    )
}

/** Un informe:  [📄]  Rendición octubre                  >
 *                     Publicado 3 nov  (o "Borrador · 1 oct – 31 oct") */
@Composable
fun FilaInforme(informe: InformeDto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val subtitulo = if (informe.publicado) "Publicado ${Fechas.diaMesDeFechaHora(informe.fechaGeneracion)}"
    else "Borrador · ${periodo(informe)}"
    FilaItem(
        titulo = informe.titulo,
        subtitulo = subtitulo,
        icono = Icons.Outlined.Description,
        colorIcono = Crema,
        modifier = modifier,
        onClick = onClick,
        derecha = { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = GrisCalido) }
    )
}

/** "1 oct – 31 oct 2026" */
fun periodo(informe: InformeDto): String {
    val ini = Fechas.leer(informe.periodoInicio)
    val fin = Fechas.leer(informe.periodoFin)
    return "${Fechas.diaMes(ini)} – ${Fechas.diaMesAnio(fin)}"
}

/** Tarjeta oscura "Saldo al cierre" con ingresos (verde) y gastos (rojo) — diseño 2, Finanzas. */
@Composable
fun TarjetaResumen(etiqueta: String, saldo: Long, ingresos: Long, gastos: Long, modifier: Modifier = Modifier) {
    Tarjeta(modifier) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
        Text(Moneda.pesos(saldo), style = MaterialTheme.typography.displaySmall, color = Crema)
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Column {
                Text("Ingresos", style = MaterialTheme.typography.bodySmall, color = VerdeOk)
                Text(Moneda.ingreso(ingresos), style = MaterialTheme.typography.titleMedium, color = VerdeOk)
            }
            Column {
                Text("Gastos", style = MaterialTheme.typography.bodySmall, color = RojoError)
                Text(Moneda.gasto(gastos), style = MaterialTheme.typography.titleMedium, color = RojoError)
            }
        }
    }
}
