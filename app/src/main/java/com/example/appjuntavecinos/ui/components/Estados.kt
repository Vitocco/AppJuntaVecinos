package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.ui.theme.Ambar
import com.example.appjuntavecinos.ui.theme.AmbarFondo
import com.example.appjuntavecinos.ui.theme.CarbonIcono
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.RojoError
import com.example.appjuntavecinos.ui.theme.RojoErrorFondo
import com.example.appjuntavecinos.ui.theme.Terracota
import com.example.appjuntavecinos.ui.theme.VerdeOk
import com.example.appjuntavecinos.ui.theme.VerdeOkFondo

// =====================================================================
// PILL DE ESTADO ("Aprobada", "Pendiente", "Pagado", "Por pagar"...)
// =====================================================================

/** Los 4 tonos posibles de una pill. */
enum class Tono(val texto: Color, val fondo: Color) {
    OK(VerdeOk, VerdeOkFondo),               // aprobada, pagado, ingreso, libre
    ERROR(RojoError, RojoErrorFondo),        // rechazada, gasto, anulado
    PENDIENTE(Ambar, AmbarFondo),            // pendiente, por pagar, ocupada
    NEUTRO(GrisCalido, CarbonIcono)          // cancelada
}

@Composable
fun PillEstado(texto: String, tono: Tono, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = tono.texto,
        modifier = modifier
            .clip(CircleShape)
            .background(tono.fondo)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

/**
 * Traduce un estado del backend (enum en MAYÚSCULAS) a texto + tono.
 * Así todas las pantallas muestran los estados igual.
 */
fun textoYTonoDe(estado: String): Pair<String, Tono> = when (estado) {
    "PENDIENTE" -> "Pendiente" to Tono.PENDIENTE
    "APROBADA" -> "Aprobada" to Tono.OK
    "RECHAZADA" -> "Rechazada" to Tono.ERROR
    "CANCELADA" -> "Cancelada" to Tono.NEUTRO
    "PAGADO" -> "Pagado" to Tono.OK
    "RECHAZADO" -> "Rechazado" to Tono.ERROR
    "VIGENTE" -> "Vigente" to Tono.OK
    "ANULADO" -> "Anulado" to Tono.NEUTRO
    else -> estado.lowercase().replaceFirstChar { it.uppercase() } to Tono.NEUTRO
}

/** Atajo: pill a partir del estado del backend. */
@Composable
fun PillEstado(estado: String, modifier: Modifier = Modifier) {
    val (texto, tono) = textoYTonoDe(estado)
    PillEstado(texto, tono, modifier)
}

// =====================================================================
// ESTADOS DE PANTALLA: cargando, vacío y error (con "Reintentar")
// =====================================================================

@Composable
fun EstadoCarga(modifier: Modifier = Modifier, texto: String = "Cargando…") {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CircularProgressIndicator(color = Terracota, strokeWidth = 3.dp)
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = GrisCalido)
    }
}

@Composable
fun EstadoVacio(
    titulo: String,
    modifier: Modifier = Modifier,
    mensaje: String? = null,
    icono: ImageVector = Icons.Outlined.Inbox
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconoCirculo(icono, GrisCalido)
        Spacer(Modifier.height(4.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, color = Crema, textAlign = TextAlign.Center)
        if (mensaje != null) {
            Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = GrisCalido, textAlign = TextAlign.Center)
        }
    }
}

/** Muestra el "mensaje" que mandó el backend y un botón para volver a intentar. */
@Composable
fun EstadoError(
    mensaje: String,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconoCirculo(Icons.Outlined.ErrorOutline, RojoError)
        Spacer(Modifier.height(4.dp))
        Text("Algo salió mal", style = MaterialTheme.typography.titleMedium, color = Crema)
        Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = GrisCalido, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        BotonPrincipal(texto = "Reintentar", onClick = onReintentar, modifier = Modifier.width(200.dp))
    }
}

/** Franja ámbar que avisa que se están mostrando datos guardados (caché de Room). */
@Composable
fun AvisoSinConexion(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AmbarFondo)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = Ambar, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            "Sin conexión · mostrando lo último guardado",
            style = MaterialTheme.typography.labelMedium,
            color = Ambar
        )
    }
}

/** Franja roja con un error general (ej: "Email o contraseña incorrectos"). Se anima al aparecer. */
@Composable
fun MensajeError(mensaje: String?, modifier: Modifier = Modifier) {
    androidx.compose.animation.AnimatedVisibility(
        visible = mensaje != null,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(RojoErrorFondo)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = RojoError, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(mensaje ?: "", style = MaterialTheme.typography.bodyMedium, color = RojoError)
        }
    }
}

@Composable
private fun IconoCirculo(icono: ImageVector, color: Color) {
    Box(
        modifier = Modifier.size(64.dp).clip(CircleShape).background(CarbonIcono),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(30.dp))
    }
}
