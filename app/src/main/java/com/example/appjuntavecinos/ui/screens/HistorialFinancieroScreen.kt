package com.example.appjuntavecinos.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.data.GastoCategoria
import com.example.appjuntavecinos.model.Informe
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.viewmodel.HistorialViewModel
import com.example.appjuntavecinos.viewmodel.MesSeleccionado
import kotlinx.coroutines.launch

// Colores propios de esta pantalla (los del mockup que no están en el tema)
private val ColorGasto = Color(0xFF8A3B12)
private val GrisSuave = Color(0xFFECEEE9)

@Composable
fun HistorialFinancieroScreen(
    modifier: Modifier = Modifier,
    viewModel: HistorialViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val c = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EncabezadoHistorial(estado.mes, viewModel.meses) { viewModel.seleccionarMes(it) }

            if (estado.cargando) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TarjetaResumen("Ingresos", estado.ingresos, c.primary, false, Modifier.weight(1f))
                    TarjetaResumen("Gastos", estado.gastos, ColorGasto, false, Modifier.weight(1f))
                    TarjetaResumen("Saldo", estado.saldo, Color.White, true, Modifier.weight(1f))
                }

                TarjetaGastos(estado.gastosPorCategoria)

                Text("Informes y rendiciones", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                estado.informes.forEach { informe ->
                    FilaInforme(informe) {
                        // Por ahora simulado; después se reemplaza por un recurso nativo
                        scope.launch { snackbarHostState.showSnackbar("Descarga simulada: ${informe.titulo}") }
                    }
                }
            }

            mensajeError?.let { Text(it, color = c.error) }
        }
    }
}

@Composable
private fun EncabezadoHistorial(
    mes: MesSeleccionado,
    meses: List<MesSeleccionado>,
    onMes: (MesSeleccionado) -> Unit
) {
    var abierto by rememberSaveable { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Historial financiero",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Box {
            OutlinedButton(onClick = { abierto = true }) {
                Text(Fechas.etiquetaMes(mes.anio, mes.mes))
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Cambiar mes")
            }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                meses.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(Fechas.etiquetaMes(m.anio, m.mes)) },
                        onClick = { onMes(m); abierto = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaResumen(
    titulo: String,
    monto: Int,
    colorMonto: Color,
    destacada: Boolean,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (destacada) c.primary else Color.White,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                titulo,
                fontSize = 12.sp,
                color = if (destacada) Color.White.copy(alpha = 0.8f) else c.onSurfaceVariant
            )
            Text(Fechas.moneda(monto), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorMonto)
        }
    }
}

@Composable
private fun TarjetaGastos(gastos: List<GastoCategoria>) {
    val c = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("¿En qué se gastó este mes?", fontWeight = FontWeight.Bold)
            if (gastos.isEmpty()) {
                Text("Sin gastos registrados este mes", color = c.onSurfaceVariant)
            } else {
                val maximo = gastos.maxOf { it.total }
                gastos.forEach { BarraGasto(it.categoria, it.total, it.total.toFloat() / maximo) }
            }
        }
    }
}

@Composable
private fun BarraGasto(categoria: String, total: Int, proporcion: Float) {
    val c = MaterialTheme.colorScheme
    // La barra se anima cuando cambia el mes (IE 2.2.2)
    val animada by animateFloatAsState(targetValue = proporcion, label = "barraGasto")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(categoria)
            Text(Fechas.moneda(total), color = c.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { animada },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
            color = c.primary,
            trackColor = GrisSuave
        )
    }
}

@Composable
private fun FilaInforme(informe: Informe, onDescargar: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (informe.esRendicion) c.primaryContainer else GrisSuave
            ) {
                Icon(
                    Icons.Outlined.Description,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.padding(10.dp).size(24.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(informe.titulo, fontWeight = FontWeight.Bold)
                val verbo = if (informe.esRendicion) "Publicada" else "Publicado"
                Text("$verbo ${Fechas.diaCorto(informe.fechaPublicacion)}", fontSize = 13.sp, color = c.onSurfaceVariant)
            }
            IconButton(onClick = onDescargar) {
                Icon(Icons.Outlined.Download, contentDescription = "Descargar ${informe.titulo}", tint = c.primary)
            }
        }
    }
}