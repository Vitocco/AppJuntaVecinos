package com.example.appjuntavecinos.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun esVertical(): Boolean =
    LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT


@Composable
fun esHorizontal(): Boolean =
    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

@Composable
fun SegunOrientacion(
    vertical: @Composable () -> Unit,
    horizontal: @Composable () -> Unit
) {
    if (esHorizontal()) horizontal() else vertical()
}

@Composable
fun DosColumnas(
    izquierda: @Composable ColumnScope.() -> Unit,
    derecha: @Composable ColumnScope.() -> Unit,
    espacio: Dp = 16.dp,
    alineacion: Alignment.Horizontal = Alignment.Start
) {
    if (esVertical()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = alineacion,
            verticalArrangement = Arrangement.spacedBy(espacio)
        ) {
            izquierda()
            derecha()
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = alineacion,
                verticalArrangement = Arrangement.spacedBy(espacio)
            ) { izquierda() }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = alineacion,
                verticalArrangement = Arrangement.spacedBy(espacio)
            ) { derecha() }
        }
    }
}