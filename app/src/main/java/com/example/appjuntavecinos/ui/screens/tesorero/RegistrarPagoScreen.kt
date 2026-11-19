package com.example.appjuntavecinos.ui.screens.tesorero

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import com.example.appjuntavecinos.ui.components.Tarjeta
import com.example.appjuntavecinos.ui.components.TituloPantalla
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.viewmodel.EstadoUi
import com.example.appjuntavecinos.ui.viewmodel.MedioPago
import com.example.appjuntavecinos.ui.viewmodel.RegistrarPagoViewModel
import com.example.appjuntavecinos.util.Fechas
import com.example.appjuntavecinos.util.SeparadorMiles
import com.example.appjuntavecinos.util.cuando

/**
 * Registrar pago (Tesorero, diseño 4): datos de la reserva, monto (precio por defecto),
 * medio Transferencia / Efectivo / Otro, fecha, referencia y comprobante OBLIGATORIO.
 * POST pagos (multipart): el backend crea el ingreso "Arriendo sede" y marca la reserva pagada.
 */
@Composable
fun RegistrarPagoScreen(
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: RegistrarPagoViewModel = viewModel()
) {
    val reserva by viewModel.reserva.collectAsState()
    val form by viewModel.form.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val guardado by viewModel.guardado.collectAsState()

    LaunchedEffect(guardado) { if (guardado) onGuardado() }

    PantallaFormulario {
        TituloPantalla("Registrar pago", onVolver = onVolver)

        when (val r = reserva) {
            EstadoUi.Cargando -> EstadoCarga()
            is EstadoUi.Error -> EstadoError(r.mensaje, onReintentar = viewModel::cargar)
            is EstadoUi.Listo -> {
                val res = r.datos
                Tarjeta {
                    Text("Reserva", style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
                    Text("${res.usuarioNombre} · ${res.motivo.orEmpty()}", style = MaterialTheme.typography.titleLarge, color = Crema)
                    Text("${res.sedeNombre} · ${res.cuando}", style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
                }

                CampoTexto(
                    etiqueta = "Monto pagado",
                    valor = form.monto,
                    onValorChange = viewModel::actualizarMonto,
                    prefijo = "$ ",
                    tipoTeclado = KeyboardType.Number,
                    estiloTexto = MaterialTheme.typography.headlineMedium,
                    transformacion = SeparadorMiles,
                    error = errores.monto
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Medio de pago", style = MaterialTheme.typography.labelMedium, color = GrisCalido)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MedioPago.entries.forEach { m ->
                            ChipFiltro(m.texto, seleccionado = m == form.medio, onClick = { viewModel.actualizarMedio(m) })
                        }
                    }
                }

                CampoFecha(
                    etiqueta = "Fecha de pago",
                    fecha = form.fecha,
                    onFecha = viewModel::actualizarFecha,
                    hasta = Fechas.hoy(),
                    error = errores.fecha
                )

                CampoTexto(
                    etiqueta = "Referencia",
                    valor = form.referencia,
                    onValorChange = viewModel::actualizarReferencia,
                    placeholder = if (form.medio == MedioPago.TRANSFERENCIA) "Ej: TRF-998877" else "Ej: Recibo N° 0002",
                    error = errores.referencia
                )

                SelectorComprobante(
                    foto = form.comprobante,
                    onFoto = viewModel::actualizarComprobante,
                    obligatorio = true,
                    error = errores.comprobante
                )

                MensajeError(mensajeError)
                BotonAccion("Guardar pago", onClick = viewModel::guardar, cargando = guardando)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
