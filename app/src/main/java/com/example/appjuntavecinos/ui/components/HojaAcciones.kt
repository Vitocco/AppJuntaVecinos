package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.CarbonIcono
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.RojoError

/**
 * Hoja inferior con acciones sobre un elemento (ej: un movimiento: Editar / Ver comprobante / Anular).
 * Cada acción es (texto, qué hacer). Las que empiezan con "Anular" se pintan en rojo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaAcciones(
    titulo: String,
    acciones: List<Pair<String, () -> Unit>>,
    onCerrar: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onCerrar, containerColor = CarbonCampo) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp)) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge,
                color = Crema,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(8.dp))
            if (acciones.isEmpty()) {
                Text(
                    "Este movimiento está anulado: no tiene acciones disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrisCalido,
                    modifier = Modifier.padding(24.dp)
                )
            }
            acciones.forEachIndexed { i, (texto, accion) ->
                if (i > 0) HorizontalDivider(color = CarbonIcono, modifier = Modifier.padding(horizontal = 24.dp))
                Text(
                    texto,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (texto.startsWith("Anular")) RojoError else Crema,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = accion)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}
