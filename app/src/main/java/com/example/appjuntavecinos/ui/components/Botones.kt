package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.ui.theme.Carbon
import com.example.appjuntavecinos.ui.theme.CarbonCampo
import com.example.appjuntavecinos.ui.theme.Crema
import com.example.appjuntavecinos.ui.theme.GrisCalido
import com.example.appjuntavecinos.ui.theme.Terracota

// ---------------------------------------------------------------------
// Botón píldora genérico. Los demás botones son "atajos" de este con
// sus colores fijos, así todas las pantallas se ven iguales.
// ---------------------------------------------------------------------
@Composable
private fun BotonPildora(
    texto: String,
    onClick: () -> Unit,
    fondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    textoDerecha: String? = null,
    cargando: Boolean = false,
    habilitado: Boolean = true,
    alto: Dp = 56.dp
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = fondo,
            contentColor = colorTexto,
            // deshabilitado o cargando: el mismo color, un poco apagado
            disabledContainerColor = fondo.copy(alpha = 0.55f),
            disabledContentColor = colorTexto.copy(alpha = 0.8f)
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
        modifier = modifier.fillMaxWidth().height(alto)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                color = colorTexto,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else if (textoDerecha != null) {
            // "Enviar solicitud ............ $ 35.000"
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(texto, style = MaterialTheme.typography.labelLarge)
                Text(textoDerecha, style = MaterialTheme.typography.labelLarge)
            }
        } else {
            if (icono != null) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Botón principal: fondo crema y texto oscuro ("Entrar", "Crear cuenta", "Solicitar arriendo"). */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    cargando: Boolean = false,
    habilitado: Boolean = true
) = BotonPildora(texto, onClick, Crema, Carbon, modifier, icono, null, cargando, habilitado)

/** Botón de acción: fondo terracota ("Enviar solicitud", "Guardar movimiento", "Pagar con Webpay"). */
@Composable
fun BotonAccion(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    textoDerecha: String? = null,
    cargando: Boolean = false,
    habilitado: Boolean = true
) = BotonPildora(texto, onClick, Terracota, Crema, modifier, icono, textoDerecha, cargando, habilitado)

/** Botón suave de color (Aprobar verde / Rechazar rojo). Más bajo que los principales. */
@Composable
fun BotonTonal(
    texto: String,
    onClick: () -> Unit,
    colorTexto: Color,
    colorFondo: Color,
    modifier: Modifier = Modifier,
    cargando: Boolean = false
) = BotonPildora(texto, onClick, colorFondo, colorTexto, modifier, cargando = cargando, alto = 46.dp)

/** Botón cuadrado de "volver" (flecha) que aparece arriba a la izquierda. */
@Composable
fun BotonVolver(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CarbonCampo.copy(alpha = 0.85f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = "Volver",
            tint = Crema
        )
    }
}

/**
 * Casilla de verificación cuadrada (relleno terracota al marcarla) con su texto.
 * Si se pasa [textoEnlace], ese trozo va en terracota y al tocarlo llama a [onEnlace]
 * (ej: "Acepto el [reglamento de uso de sedes]"). Tocar el resto marca/desmarca.
 */
@Composable
fun CasillaVerificacion(
    texto: String,
    marcada: Boolean,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    textoEnlace: String? = null,
    onEnlace: () -> Unit = {}
) {
    androidx.compose.foundation.layout.Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onCambio(!marcada) }
                .padding(vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (marcada) Terracota else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (marcada) Terracota else if (error != null) com.example.appjuntavecinos.ui.theme.RojoError else GrisCalido,
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (marcada) Icon(Icons.Filled.Check, null, tint = Crema, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = buildAnnotatedString {
                    append(texto)
                    if (textoEnlace != null) {
                        // LinkAnnotation.Clickable: solo este trozo responde al toque
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = "enlace",
                                styles = TextLinkStyles(SpanStyle(color = Terracota, fontWeight = FontWeight.SemiBold))
                            ) { onEnlace() }
                        ) { append(textoEnlace) }
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Crema
            )
        }
        if (error != null) {
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = com.example.appjuntavecinos.ui.theme.RojoError,
                modifier = Modifier.padding(start = 34.dp)
            )
        }
    }
}
