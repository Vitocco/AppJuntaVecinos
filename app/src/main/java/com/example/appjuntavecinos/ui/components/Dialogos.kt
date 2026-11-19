package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.example.appjuntavecinos.ui.theme.CarbonCampoOn
import com.example.appjuntavecinos.ui.theme.CarbonIcono
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.RojoError
import com.example.appjuntavecinos.ui.theme.RojoErrorFondo

/** Caja base de todos los diálogos: fondo CarbonCampo y esquinas de tarjeta (24dp). */
@Composable
private fun CajaDialogo(onCerrar: () -> Unit, contenido: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onCerrar) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                // un tono más claro que los campos, para que el campo de texto se distinga
                .background(CarbonCampoOn)
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) { contenido() }
    }
}

/**
 * Confirmación propia (no el AlertDialog gris de Android).
 * [peligroso] = true pinta el botón en rojo (cancelar reserva, anular movimiento).
 */
@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    peligroso: Boolean = false,
    cargando: Boolean = false,
    textoCancelar: String = "Volver"
) {
    CajaDialogo(onCerrar = onCancelar) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall, color = Crema)
        Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 6.dp)) {
            BotonTonal(textoCancelar, onClick = onCancelar, colorTexto = Crema, colorFondo = CarbonIcono, modifier = Modifier.weight(1f))
            if (peligroso) {
                BotonTonal(textoConfirmar, onClick = onConfirmar, colorTexto = RojoError, colorFondo = RojoErrorFondo,
                    modifier = Modifier.weight(1f), cargando = cargando)
            } else {
                BotonTonal(textoConfirmar, onClick = onConfirmar, colorTexto = Crema,
                    colorFondo = com.example.appjuntavecinos.ui.theme.Terracota, modifier = Modifier.weight(1f), cargando = cargando)
            }
        }
    }
}

/**
 * Aprobar / Rechazar una solicitud con observación OPCIONAL.
 * Ej: "Aprobada, recuerde dejar el salón limpio".
 */
@Composable
fun DialogoObservacion(
    titulo: String,
    detalle: String,
    textoConfirmar: String,
    aprobar: Boolean,
    onConfirmar: (observacion: String) -> Unit,
    onCancelar: () -> Unit,
    cargando: Boolean = false
) {
    var observacion by rememberSaveable { mutableStateOf("") }
    CajaDialogo(onCerrar = onCancelar) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall, color = Crema)
        Text(detalle, style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
        CampoTexto(
            etiqueta = "Observación (opcional)",
            valor = observacion,
            onValorChange = { if (it.length <= 200) observacion = it },
            placeholder = if (aprobar) "Ej: Dejar el espacio limpio" else "Ej: Se cruza con otra actividad",
            unaLinea = false,
            minLineas = 2
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotonTonal("Volver", onClick = onCancelar, colorTexto = Crema, colorFondo = CarbonIcono, modifier = Modifier.weight(1f))
            val tono = if (aprobar) Tono.OK else Tono.ERROR
            BotonTonal(textoConfirmar, onClick = { onConfirmar(observacion.trim()) },
                colorTexto = tono.texto, colorFondo = tono.fondo, modifier = Modifier.weight(1f), cargando = cargando)
        }
    }
}

/** Muestra la imagen del comprobante de un movimiento (Coil, con token). */
@Composable
fun DialogoComprobante(url: String, onCerrar: () -> Unit) {
    CajaDialogo(onCerrar = onCerrar) {
        Text("Comprobante", style = MaterialTheme.typography.headlineSmall, color = Crema)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 460.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(CarbonIcono),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = "Imagen del comprobante",
                contentScale = ContentScale.Fit,
                loading = { EstadoCarga(texto = "Cargando imagen…") },
                error = { Text("No se pudo cargar el comprobante", color = GrisCalido, modifier = Modifier.padding(20.dp)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        TextButton(onClick = onCerrar, modifier = Modifier.align(Alignment.End)) {
            Text("Cerrar", color = Crema)
        }
    }
}
