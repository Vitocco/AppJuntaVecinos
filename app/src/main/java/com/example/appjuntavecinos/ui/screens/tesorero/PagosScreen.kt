package com.example.appjuntavecinos.ui.screens.tesorero

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.AlVolverAPantalla
import com.example.appjuntavecinos.ui.components.ChipFiltro
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.PantallaLista
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.components.seccion
import com.example.appjuntavecinos.ui.viewmodel.FiltroPagos
import com.example.appjuntavecinos.ui.viewmodel.PagosViewModel

/**
 * Pagos de reservas (Tesorero, diseño 4): pagos registrados (GET pagos) y reservas
 * APROBADAS no pagadas ("Por pagar") con su botón "Registrar pago".
 */
@Composable
fun PagosScreen(
    onRegistrarPago: (Long) -> Unit,
    viewModel: PagosViewModel = viewModel()
) {
    val filtro by viewModel.filtro.collectAsState()
    val estado by viewModel.estado.collectAsState()
    AlVolverAPantalla { viewModel.cargar() }

    PantallaLista(onRefrescar = viewModel::cargar) {
        item { TextoConPunto("Pagos de reservas", estilo = MaterialTheme.typography.displaySmall) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FiltroPagos.entries.forEach { f ->
                    ChipFiltro(f.texto, seleccionado = f == filtro, onClick = { viewModel.elegirFiltro(f) })
                }
            }
        }
        seccion(estado, viewModel::cargar) { todos ->
            val lista = todos.filter { filtro.aplicar(it) }
            if (lista.isEmpty()) item {
                EstadoVacio(
                    when (filtro) {
                        FiltroPagos.POR_PAGAR -> "No hay reservas por pagar"
                        FiltroPagos.PAGADOS -> "Aún no hay pagos registrados"
                        FiltroPagos.TODOS -> "Sin pagos ni reservas por cobrar"
                    }
                )
            }
            items(lista, key = { it.clave }) { FilaPago(it, onRegistrarPago) }
        }
    }
}
