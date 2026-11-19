package com.example.appjuntavecinos.ui.screens.comun

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.data.remote.ApiConfig
import com.example.appjuntavecinos.ui.components.AlVolverAPantalla
import com.example.appjuntavecinos.ui.components.BarrasGastos
import com.example.appjuntavecinos.ui.components.ChipsMes
import com.example.appjuntavecinos.ui.components.DialogoComprobante
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.FilaInforme
import com.example.appjuntavecinos.ui.components.FilaMovimiento
import com.example.appjuntavecinos.ui.components.PantallaLista
import com.example.appjuntavecinos.ui.components.TarjetaResumen
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.components.TituloSeccion
import com.example.appjuntavecinos.ui.components.seccion
import com.example.appjuntavecinos.ui.viewmodel.FinanzasViewModel
import com.example.appjuntavecinos.util.Fechas

/**
 * Finanzas (Vecino y Directiva, diseño 2 "Historial financiero"):
 * chips de mes, saldo al cierre, ingresos/gastos, barras por categoría,
 * movimientos (solo Directiva, con su comprobante) e informes publicados.
 */
@Composable
fun FinanzasScreen(
    onInforme: (Long) -> Unit,
    viewModel: FinanzasViewModel = viewModel()
) {
    val mes by viewModel.mes.collectAsState()
    val resumen by viewModel.resumen.collectAsState()
    val movimientos by viewModel.movimientos.collectAsState()
    val informes by viewModel.informes.collectAsState()

    // id del movimiento cuyo comprobante se está mirando (null = ninguno)
    var comprobanteDe by rememberSaveable { mutableStateOf<Long?>(null) }
    comprobanteDe?.let { id ->
        DialogoComprobante(url = ApiConfig.urlComprobante(id), onCerrar = { comprobanteDe = null })
    }

    AlVolverAPantalla { viewModel.cargar() }

    PantallaLista(onRefrescar = viewModel::cargar) {
        item { TextoConPunto("Finanzas", estilo = MaterialTheme.typography.displaySmall) }
        item { ChipsMes(viewModel.meses, mes, viewModel::seleccionarMes) }

        seccion(resumen, viewModel::cargar) { r ->
            item {
                TarjetaResumen(
                    etiqueta = "Saldo al cierre · ${Fechas.nombreMes(mes)}",
                    saldo = r.saldo,
                    ingresos = r.ingresosMes,
                    gastos = r.gastosMes
                )
            }
            item { TituloSeccion("¿En qué se gastó este mes?") }
            item { BarrasGastos(r.gastosPorCategoria.orEmpty()) }
        }

        if (viewModel.verMovimientos) {
            item { TituloSeccion("Movimientos del mes") }
            seccion(movimientos, viewModel::cargar) { lista ->
                if (lista.isEmpty()) item { EstadoVacio("Sin movimientos este mes") }
                items(lista, key = { it.id }) { m ->
                    FilaMovimiento(
                        movimiento = m,
                        // tocar un movimiento con comprobante abre la imagen
                        onClick = if (m.tieneComprobante) ({ comprobanteDe = m.id }) else null
                    )
                }
            }
        }

        item { TituloSeccion("Informes y rendiciones") }
        seccion(informes, viewModel::cargar) { lista ->
            if (lista.isEmpty()) item { EstadoVacio("Aún no hay informes publicados") }
            items(lista, key = { it.id }) { inf -> FilaInforme(inf, onClick = { onInforme(inf.id) }) }
        }
    }
}
