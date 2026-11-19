package com.example.appjuntavecinos.ui.screens.tesorero

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.data.remote.ApiConfig
import com.example.appjuntavecinos.data.remote.dto.MovimientoDto
import com.example.appjuntavecinos.ui.components.AlVolverAPantalla
import com.example.appjuntavecinos.ui.components.ChipsMes
import com.example.appjuntavecinos.ui.components.DialogoComprobante
import com.example.appjuntavecinos.ui.components.DialogoConfirmacion
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.FilaMovimiento
import com.example.appjuntavecinos.ui.components.HojaAcciones
import com.example.appjuntavecinos.ui.components.MostrarMensaje
import com.example.appjuntavecinos.ui.components.PantallaLista
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.Terracota
import com.example.appjuntavecinos.ui.components.seccion
import com.example.appjuntavecinos.ui.viewmodel.MovimientosViewModel
import com.example.appjuntavecinos.util.Moneda

/**
 * Movimientos (Tesorero): lista del mes elegido; al tocar uno se puede editar,
 * anular (con confirmación) o ver su comprobante. Los anulados aparecen grises y tachados.
 */
@Composable
fun MovimientosScreen(
    onNuevo: () -> Unit,
    onEditar: (MovimientoDto) -> Unit,
    viewModel: MovimientosViewModel = viewModel()
) {
    val mes by viewModel.mes.collectAsState()
    val estado by viewModel.estado.collectAsState()
    val elegido by viewModel.elegido.collectAsState()
    val porAnular by viewModel.porAnular.collectAsState()
    val anulando by viewModel.anulando.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    var comprobanteDe by rememberSaveable { mutableStateOf<Long?>(null) }

    AlVolverAPantalla { viewModel.cargar() }
    MostrarMensaje(mensaje, viewModel::mensajeMostrado)

    comprobanteDe?.let { DialogoComprobante(ApiConfig.urlComprobante(it), onCerrar = { comprobanteDe = null }) }

    // Menú de acciones del movimiento tocado
    elegido?.let { m ->
        val vigente = m.estado == "VIGENTE"
        HojaAcciones(
            titulo = m.descripcion ?: m.categoriaNombre.orEmpty(),
            acciones = buildList {
                if (vigente) add("Editar" to { viewModel.cerrarAcciones(); onEditar(m) })
                if (m.tieneComprobante) add("Ver comprobante" to { viewModel.cerrarAcciones(); comprobanteDe = m.id })
                if (vigente) add("Anular movimiento" to { viewModel.pedirAnular(m) })
            },
            onCerrar = viewModel::cerrarAcciones
        )
    }

    porAnular?.let { m ->
        DialogoConfirmacion(
            titulo = "¿Anular movimiento?",
            mensaje = "${m.descripcion} · ${Moneda.pesos(m.monto)}. Quedará en el historial como ANULADO y no sumará al saldo.",
            textoConfirmar = "Anular",
            peligroso = true,
            cargando = anulando,
            onConfirmar = viewModel::confirmarAnular,
            onCancelar = viewModel::descartarAnular
        )
    }

    PantallaLista(
        onRefrescar = viewModel::cargar,
        botonFlotante = {
            FloatingActionButton(onClick = onNuevo, containerColor = Terracota, contentColor = Crema) {
                Icon(Icons.Outlined.Add, contentDescription = "Registrar movimiento")
            }
        }
    ) {
        item { TextoConPunto("Movimientos", estilo = MaterialTheme.typography.displaySmall) }
        item { ChipsMes(viewModel.meses, mes, viewModel::seleccionarMes) }
        seccion(estado, viewModel::cargar) { lista ->
            if (lista.isEmpty()) item { EstadoVacio("Sin movimientos este mes", mensaje = "Toca + para registrar uno.") }
            items(lista, key = { it.id }) { m ->
                FilaMovimiento(m, onClick = { viewModel.abrirAcciones(m) })
            }
        }
    }
}
