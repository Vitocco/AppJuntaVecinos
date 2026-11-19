package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.ui.theme.BarraNav
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.Terracota

/** Una pestaña de la barra: a qué ruta lleva, su texto y su ícono. */
data class Pestana(val ruta: String, val etiqueta: String, val icono: ImageVector)

/**
 * Barra inferior única para los 3 roles: cada rol le pasa SU lista de pestañas.
 * La pestaña activa se pinta terracota y las demás en gris cálido.
 */
@Composable
fun BarraInferior(
    pestanas: List<Pestana>,
    rutaActual: String?,
    onSeleccionar: (Pestana) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(BarraNav)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        HorizontalDivider(color = CarbonCampo, thickness = 1.dp)
        Row(
            modifier = Modifier.fillMaxWidth().height(68.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            pestanas.forEach { pestana ->
                val activa = pestana.ruta == rutaActual
                val color = if (activa) Terracota else GrisCalido
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null    // sin el "ripple" gris, como en el diseño
                        ) { onSeleccionar(pestana) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(pestana.icono, contentDescription = pestana.etiqueta, tint = color, modifier = Modifier.size(24.dp))
                    Text(
                        pestana.etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
