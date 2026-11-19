package com.example.appjuntavecinos.ui.screens.comun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.BarrasGastos
import com.example.appjuntavecinos.ui.components.BotonVolver
import com.example.appjuntavecinos.ui.components.EstadoCarga
import com.example.appjuntavecinos.ui.components.EstadoError
import com.example.appjuntavecinos.ui.components.PantallaFormulario
import com.example.appjuntavecinos.ui.components.PillEstado
import com.example.appjuntavecinos.ui.components.Tarjeta
import com.example.appjuntavecinos.ui.components.TextoConPunto
import com.example.appjuntavecinos.ui.components.TituloSeccion
import com.example.appjuntavecinos.ui.components.Tono
import com.example.appjuntavecinos.ui.components.periodo
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.RojoError
import com.example.appjuntavecinos.ui.theme.VerdeOk
import com.example.appjuntavecinos.ui.viewmodel.EstadoUi
import com.example.appjuntavecinos.ui.viewmodel.InformeDetalleViewModel
import com.example.appjuntavecinos.util.Moneda

/** Detalle de un informe / rendición: período, totales y gastos por categoría. */
@Composable
fun InformeDetalleScreen(
    onVolver: () -> Unit,
    viewModel: InformeDetalleViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    PantallaFormulario {
        BotonVolver(onClick = onVolver)
        when (val e = estado) {
            EstadoUi.Cargando -> EstadoCarga()
            is EstadoUi.Error -> EstadoError(e.mensaje, onReintentar = viewModel::cargar)
            is EstadoUi.Listo -> {
                val inf = e.datos
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextoConPunto(inf.titulo, estilo = MaterialTheme.typography.displaySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(periodo(inf), style = MaterialTheme.typography.bodyLarge, color = GrisCalido)
                        if (inf.publicado) PillEstado("Publicado", Tono.OK) else PillEstado("Borrador", Tono.PENDIENTE)
                    }
                }
                if (!inf.observacion.isNullOrBlank()) {
                    Text(inf.observacion, style = MaterialTheme.typography.bodyMedium, color = Crema)
                }
                Tarjeta {
                    FilaTotal("Total ingresos", Moneda.ingreso(inf.totalIngresos ?: 0), VerdeOk)
                    FilaTotal("Total gastos", Moneda.gasto(inf.totalGastos ?: 0), RojoError)
                    FilaTotal("Saldo del período", Moneda.pesos(inf.saldoPeriodo ?: 0), Crema)
                }
                TituloSeccion("Gastos por categoría")
                BarrasGastos(inf.gastosPorCategoria.orEmpty())
            }
        }
    }
}

@Composable
private fun FilaTotal(etiqueta: String, valor: String, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = MaterialTheme.typography.bodyLarge, color = GrisCalido)
        Text(valor, style = MaterialTheme.typography.titleMedium, color = color)
    }
}
