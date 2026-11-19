package com.example.appjuntavecinos.ui.screens.tesorero

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.BotonAccion
import com.example.appjuntavecinos.ui.components.CampoFecha
import com.example.appjuntavecinos.ui.components.CampoTexto
import com.example.appjuntavecinos.ui.components.ChipFiltro
import com.example.appjuntavecinos.ui.components.EstadoCarga
import com.example.appjuntavecinos.ui.components.EstadoError
import com.example.appjuntavecinos.ui.components.MensajeError
import com.example.appjuntavecinos.ui.components.PantallaFormulario
import com.example.appjuntavecinos.ui.components.SelectorComprobante
import com.example.appjuntavecinos.ui.components.SelectorSegmentado
import com.example.appjuntavecinos.ui.components.TituloPantalla
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.RojoError
import com.example.appjuntavecinos.ui.viewmodel.EstadoUi
import com.example.appjuntavecinos.ui.viewmodel.RegistrarMovimientoViewModel
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.SeparadorMiles

/**
 * Registrar / editar movimiento (diseño 2): Ingreso | Gasto, monto grande,
 * categorías del tipo elegido (GET categorias), detalle, fecha y comprobante (cámara o galería).
 */
@Composable
fun RegistrarMovimientoScreen(
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: RegistrarMovimientoViewModel = viewModel()
) {
    val categorias by viewModel.categorias.collectAsState()
    val form by viewModel.form.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val guardado by viewModel.guardado.collectAsState()

    LaunchedEffect(guardado) { if (guardado) onGuardado() }

    PantallaFormulario {
        TituloPantalla(if (viewModel.esEdicion) "Editar movimiento" else "Registrar movimiento", onVolver = onVolver)

        when (val c = categorias) {
            EstadoUi.Cargando -> EstadoCarga()
            is EstadoUi.Error -> EstadoError(c.mensaje, onReintentar = viewModel::cargar)
            is EstadoUi.Listo -> {
                val tipos = listOf("INGRESO", "GASTO")
                SelectorSegmentado(
                    opciones = listOf("Ingreso", "Gasto"),
                    seleccionada = tipos.indexOf(form.tipo),
                    onSeleccionar = { viewModel.actualizarTipo(tipos[it]) }
                )

                CampoTexto(
                    etiqueta = "Monto (CLP)",
                    valor = form.monto,
                    onValorChange = viewModel::actualizarMonto,
                    placeholder = "0",
                    prefijo = "$ ",
                    tipoTeclado = KeyboardType.Number,
                    estiloTexto = MaterialTheme.typography.headlineMedium,
                    transformacion = SeparadorMiles,
                    error = errores.monto
                )

                // Categorías: solo las del tipo elegido
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Categoría", style = MaterialTheme.typography.labelMedium, color = GrisCalido)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        c.datos.filter { it.tipo == form.tipo }.forEach { cat ->
                            ChipFiltro(cat.nombre, seleccionado = cat.id == form.categoriaId, onClick = { viewModel.actualizarCategoria(cat.id) })
                        }
                    }
                    errores.categoria?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = RojoError, modifier = Modifier.padding(start = 4.dp))
                    }
                }

                CampoTexto(
                    etiqueta = "Detalle",
                    valor = form.detalle,
                    onValorChange = viewModel::actualizarDetalle,
                    placeholder = if (form.tipo == "INGRESO") "Ej: cuotas sociales noviembre" else "Ej: boleta de luz",
                    error = errores.detalle
                )

                CampoFecha(
                    etiqueta = "Fecha",
                    fecha = form.fecha,
                    onFecha = viewModel::actualizarFecha,
                    hasta = Fechas.hoy(),
                    error = errores.fecha
                )

                // Al editar no se cambia la foto (PUT solo recibe JSON)
                if (!viewModel.esEdicion) {
                    SelectorComprobante(
                        foto = form.comprobante,
                        onFoto = viewModel::actualizarComprobante,
                        abrirCamaraAlInicio = viewModel.abrirCamara
                    )
                }

                MensajeError(mensajeError)
                BotonAccion(
                    texto = if (viewModel.esEdicion) "Guardar cambios" else "Guardar movimiento",
                    onClick = viewModel::guardar,
                    cargando = guardando
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
