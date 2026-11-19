package com.example.appjuntavecinos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.Terracota

/**
 * Selector de 2 o más opciones en una sola píldora (ej: Ingreso | Gasto).
 * La opción elegida se pinta terracota.
 */
@Composable
fun SelectorSegmentado(
    opciones: List<String>,
    seleccionada: Int,
    onSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(CarbonCampo)
            .padding(4.dp)
    ) {
        opciones.forEachIndexed { indice, texto ->
            val elegida = indice == seleccionada
            val fondo by animateColorAsState(if (elegida) Terracota else Color.Transparent, label = "fondo")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(fondo)
                    .clickable { onSeleccionar(indice) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    texto,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (elegida) Crema else GrisCalido
                )
            }
        }
    }
}

/** Chip redondo para filtrar (Todas / Pendientes, meses, categorías, medios de pago). */
@Composable
fun ChipFiltro(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fondo by animateColorAsState(if (seleccionado) Terracota else CarbonCampo, label = "chip")
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(fondo)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            color = if (seleccionado) Crema else GrisCalido
        )
    }
}
