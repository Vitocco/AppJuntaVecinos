package com.example.appjuntavecinos.ui.screens.tesorero

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.AlVolverAPantalla
import com.example.appjuntavecinos.ui.components.EncabezadoHome
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.FilaMovimiento
import com.example.appjuntavecinos.ui.components.PantallaLista
import com.example.appjuntavecinos.ui.components.Tarjeta
import com.example.appjuntavecinos.ui.components.TarjetaSaldo
import com.example.appjuntavecinos.ui.components.TituloSeccion
import com.example.appjuntavecinos.ui.components.seccion
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.RojoError
import com.example.appjuntavecinos.ui.theme.TerracotaSuave
import com.example.appjuntavecinos.ui.theme.VerdeOk
import com.example.appjuntavecinos.ui.viewmodel.TesoreroInicioViewModel
import com.example.appjuntavecinos.util.Fechas

/**
 * Inicio del Tesorero (diseño 3): saldo, accesos "+ Ingreso / − Gasto / Boleta",
 * pagos de reservas (pagados y por pagar) y últimos movimientos.
 */
@Composable
fun TesoreroInicioScreen(
    onIngreso: () -> Unit,
    onGasto: () -> Unit,
    onBoleta: () -> Unit,
    onVerPagos: () -> Unit,
    onVerMovimientos: () -> Unit,
    onRegistrarPago: (Long) -> Unit,
    viewModel: TesoreroInicioViewModel = viewModel()
) {
    val sesion by viewModel.sesion.collectAsState()
    val resumen by viewModel.resumen.collectAsState()
    val pagos by viewModel.pagos.collectAsState()
    val movimientos by viewModel.movimientos.collectAsState()

    AlVolverAPantalla { viewModel.cargar() }

    PantallaLista(onRefrescar = viewModel::cargar) {
        item { EncabezadoHome(saludo = "Hola, ${sesion?.primerNombre.orEmpty()}", titulo = "Tesorería", rol = "TESORERO") }

        seccion(resumen, viewModel::cargar) { r ->
            item {
                TarjetaSaldo("Saldo de la junta · ${Fechas.nombreMes(viewModel.mes)}", r.saldo, r.ingresosMes, r.gastosMes)
            }
        }

        // Accesos rápidos
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Acceso("Ingreso", Icons.Outlined.Add, VerdeOk, onIngreso, Modifier.weight(1f))
                Acceso("Gasto", Icons.Outlined.Remove, RojoError, onGasto, Modifier.weight(1f))
                Acceso("Boleta", Icons.Outlined.PhotoCamera, TerracotaSuave, onBoleta, Modifier.weight(1f))
            }
        }

        item { TituloSeccion("Pagos de reservas", accion = "Ver todos", onAccion = onVerPagos) }
        seccion(pagos, viewModel::cargar) { lista ->
            if (lista.isEmpty()) item { EstadoVacio("Sin pagos ni reservas por cobrar") }
            items(lista, key = { it.clave }) { FilaPago(it, onRegistrarPago) }
        }

        item { TituloSeccion("Últimos movimientos", accion = "Ver todos", onAccion = onVerMovimientos) }
        seccion(movimientos, viewModel::cargar) { lista ->
            if (lista.isEmpty()) item { EstadoVacio("Sin movimientos este mes") }
            items(lista, key = { it.id }) { FilaMovimiento(it) }
        }
    }
}

@Composable
private fun Acceso(texto: String, icono: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Tarjeta(modifier, onClick = onClick) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(26.dp).align(Alignment.CenterHorizontally))
        Text(
            texto, style = MaterialTheme.typography.bodyMedium, color = Crema,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
        )
    }
}
