package com.example.appjuntavecinos.ui.screens.directiva

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.AlVolverAPantalla
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.FotoSede
import com.example.appjuntavecinos.ui.components.PantallaLista
import com.example.appjuntavecinos.ui.components.PillEstado
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.components.Tono
import com.example.appjuntavecinos.ui.components.seccion
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.TerracotaSuave
import com.example.appjuntavecinos.ui.viewmodel.CalendarioViewModel
import com.example.appjuntavecinos.ui.viewmodel.SedeHoy

/** Calendario (Directiva, diseño 5): una tarjeta con foto por sede y su estado de hoy. */
@Composable
fun CalendarioScreen(
    onSede: (Long) -> Unit,
    viewModel: CalendarioViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    AlVolverAPantalla { viewModel.cargar() }

    PantallaLista(onRefrescar = viewModel::cargar) {
        item { TextoConPunto("Sedes y calendario", estilo = MaterialTheme.typography.displaySmall) }
        seccion(estado, viewModel::cargar) { sedes ->
            if (sedes.isEmpty()) item { EstadoVacio("No hay sedes registradas") }
            items(sedes, key = { it.sede.id }) { s -> TarjetaSede(s) { onSede(s.sede.id) } }
        }
    }
}

@Composable
private fun TarjetaSede(item: SedeHoy, onClick: () -> Unit) {
    val sede = item.sede
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(CarbonCampo)
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.fillMaxWidth().height(170.dp)) {
            FotoSede(ruta = sede.fotos?.firstOrNull(), descripcion = sede.nombre, modifier = Modifier.fillMaxWidth().height(170.dp))
            val pill = if (item.ocupadaDesde == null) "Libre hoy" to Tono.OK else "Ocupada ${item.ocupadaDesde}" to Tono.PENDIENTE
            PillEstado(pill.first, pill.second, Modifier.align(Alignment.TopEnd).padding(12.dp))
        }
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(sede.nombre, style = MaterialTheme.typography.titleLarge, color = Crema, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.Groups, contentDescription = "Capacidad", tint = TerracotaSuave, modifier = Modifier.size(20.dp))
                Text(" ${sede.capacidad}", style = MaterialTheme.typography.titleMedium, color = GrisCalido)
            }
            Text(sede.ubicacion.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
        }
    }
}
