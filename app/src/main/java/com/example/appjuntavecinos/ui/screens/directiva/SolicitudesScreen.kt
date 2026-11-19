package com.example.appjuntavecinos.ui.screens.directiva

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.AlVolverAPantalla
import com.example.appjuntavecinos.ui.components.ChipFiltro
import com.example.appjuntavecinos.ui.components.DialogoDecision
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.MostrarMensaje
import com.example.appjuntavecinos.ui.components.PantallaLista
import com.example.appjuntavecinos.ui.components.TarjetaSolicitud
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.components.seccion
import com.example.appjuntavecinos.ui.viewmodel.FiltroSolicitudes
import com.example.appjuntavecinos.ui.viewmodel.SolicitudesViewModel

/** Solicitudes (Directiva): GET reservas?estado= con un chip por estado. */
@Composable
fun SolicitudesScreen(viewModel: SolicitudesViewModel = viewModel()) {
    val filtro by viewModel.filtro.collectAsState()
    val estado by viewModel.estado.collectAsState()
    val porDecidir by viewModel.porDecidir.collectAsState()
    val decidiendo by viewModel.decidiendo.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    AlVolverAPantalla { viewModel.cargar() }
    MostrarMensaje(mensaje, viewModel::mensajeMostrado)
    porDecidir?.let {
        DialogoDecision(it, decidiendo, viewModel::confirmarDecision, viewModel::descartarDecision)
    }

    PantallaLista(onRefrescar = viewModel::cargar) {
        item { TextoConPunto("Solicitudes", estilo = MaterialTheme.typography.displaySmall) }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FiltroSolicitudes.entries.forEach { f ->
                    ChipFiltro(f.texto, seleccionado = f == filtro, onClick = { viewModel.elegirFiltro(f) })
                }
            }
        }
        seccion(estado, viewModel::cargar) { lista ->
            if (lista.isEmpty()) item { EstadoVacio("No hay solicitudes ${filtro.texto.lowercase()}") }
            items(lista, key = { it.id }) { r ->
                TarjetaSolicitud(r, onAprobar = { viewModel.aprobar(r) }, onRechazar = { viewModel.rechazar(r) })
            }
        }
    }
}
