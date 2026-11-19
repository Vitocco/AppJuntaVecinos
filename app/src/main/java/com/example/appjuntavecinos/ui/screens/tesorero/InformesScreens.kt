package com.example.appjuntavecinos.ui.screens.tesorero

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.AlVolverAPantalla
import com.example.appjuntavecinos.ui.components.BotonAccion
import com.example.appjuntavecinos.ui.components.CampoFecha
import com.example.appjuntavecinos.ui.components.CampoTexto
import com.example.appjuntavecinos.ui.components.CasillaVerificacion
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.FilaInforme
import com.example.appjuntavecinos.ui.components.MensajeError
import com.example.appjuntavecinos.ui.components.PantallaFormulario
import com.example.appjuntavecinos.ui.components.PantallaLista
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.components.TituloPantalla
import com.example.appjuntavecinos.ui.components.seccion
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.Terracota
import com.example.appjuntavecinos.ui.viewmodel.InformesViewModel
import com.example.appjuntavecinos.ui.viewmodel.NuevoInformeViewModel

/** Informes (Tesorero): todos los informes (también borradores) y botón + para crear uno. */
@Composable
fun InformesScreen(
    onInforme: (Long) -> Unit,
    onNuevo: () -> Unit,
    viewModel: InformesViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    AlVolverAPantalla { viewModel.cargar() }

    PantallaLista(
        onRefrescar = viewModel::cargar,
        botonFlotante = {
            FloatingActionButton(onClick = onNuevo, containerColor = Terracota, contentColor = Crema) {
                Icon(Icons.Outlined.Add, contentDescription = "Nuevo informe")
            }
        }
    ) {
        item { TextoConPunto("Informes", estilo = MaterialTheme.typography.displaySmall) }
        seccion(estado, viewModel::cargar) { lista ->
            if (lista.isEmpty()) item { EstadoVacio("Aún no hay informes", mensaje = "Toca + para crear la primera rendición.") }
            items(lista, key = { it.id }) { inf -> FilaInforme(inf, onClick = { onInforme(inf.id) }) }
        }
    }
}

/**
 * Nuevo informe (POST informes): título, período (desde / hasta), observación y si se publica
 * (los vecinos solo ven los publicados). El backend calcula los totales del período.
 */
@Composable
fun NuevoInformeScreen(
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: NuevoInformeViewModel = viewModel()
) {
    val form by viewModel.form.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val guardado by viewModel.guardado.collectAsState()

    LaunchedEffect(guardado) { if (guardado) onGuardado() }

    PantallaFormulario {
        TituloPantalla("Nuevo informe", onVolver = onVolver)
        CampoTexto(
            etiqueta = "Título",
            valor = form.titulo,
            onValorChange = viewModel::actualizarTitulo,
            placeholder = "Ej: Rendición noviembre",
            error = errores.titulo
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            CampoFecha("Desde", form.inicio, viewModel::actualizarInicio, Modifier.weight(1f), errores.inicio)
            CampoFecha("Hasta", form.fin, viewModel::actualizarFin, Modifier.weight(1f), errores.fin, desde = form.inicio)
        }
        CampoTexto(
            etiqueta = "Observación",
            valor = form.observacion,
            onValorChange = viewModel::actualizarObservacion,
            placeholder = "Ej: Presentada en la asamblea de diciembre",
            unaLinea = false,
            minLineas = 3
        )
        CasillaVerificacion(
            texto = "Publicar ahora (visible para los vecinos)",
            marcada = form.publicado,
            onCambio = viewModel::actualizarPublicado
        )
        MensajeError(mensajeError)
        BotonAccion("Crear informe", onClick = viewModel::guardar, cargando = guardando)
        Spacer(Modifier.height(12.dp))
    }
}
